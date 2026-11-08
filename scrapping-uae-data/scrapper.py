#!/usr/bin/env python3
"""Collect public UAE facts and source links for the CloudSim project."""

import argparse
from datetime import datetime, timezone
import json
import re
import sys
import time
import warnings
from pathlib import Path
from urllib.parse import urljoin, urlparse

import requests
import urllib3
from bs4 import BeautifulSoup


BASE_URL = "https://www.uae.ac.ma"
USER_AGENT = "UAE-CloudSim-research-scraper/1.0 (educational use)"
REQUEST_TIMEOUT = 25
DEFAULT_OUTPUT_DIR = Path(__file__).resolve().parent / "data"

RESEARCH_PAGES = {
    "homepage": "/",
    "establishments": "/etablissements",
    "history": "/histoire",
    "timeline": "/chronologie",
    "academic_calendar": "/calendriers-universitaires",
    "press_releases": "/communiques-presse",
    "pedagogical_innovation": "/centre-de-linnovation-pedagogique",
    "digital_strategy": "/strategie-pacte-esri",
    "development_project": "/projet-de-developpement",
    "infrastructure": "/infrastructures",
}

PRESS_PAGE_COUNT = 3


def clean_text(element):
    return " ".join(element.stripped_strings)


def normalize_number(value):
    normalized = value.replace("\u202f", "").replace("\xa0", "").replace(" ", "")
    if "," in normalized:
        normalized = normalized.replace(".", "").replace(",", ".")
        try:
            number = float(normalized)
            return int(number) if number.is_integer() else number
        except ValueError:
            return None

    normalized = normalized.replace(".", "")
    try:
        return int(normalized)
    except ValueError:
        return None


def extract_statistics(soup, source_url):
    statistics = []
    seen = set()

    for counter in soup.select(".statics-chiffre .bor, .statics-chiffre .counter"):
        value_element = counter.select_one(".counter, .chiffre")
        if value_element is None and "counter" in counter.get("class", []):
            value_element = counter
        if value_element is None:
            continue

        value_text = clean_text(value_element)
        label_element = counter.select_one("p")
        label = clean_text(label_element) if label_element else ""
        numeric_value = normalize_number(value_text)
        if (
            numeric_value is None
            or len(label) < 2
            or (value_text, label) in seen
        ):
            continue

        seen.add((value_text, label))
        statistics.append(
            {
                "value": numeric_value,
                "value_display": value_text,
                "label": label,
                "source_url": source_url,
                "reference_date": None,
            }
        )

    return statistics


def extract_menu(soup):
    menu = []
    for item in soup.select("ul.navbar-nav > li.mega-dropdown"):
        title = item.select_one("a.nav-link")
        categories = []
        for header in item.select("li.dropdown-header"):
            category_title = header.select_one("span")
            links = [
                {
                    "title": clean_text(anchor),
                    "url": urljoin(BASE_URL, anchor.get("href", "")),
                }
                for anchor in header.select("ul li a[href]")
            ]
            categories.append(
                {
                    "category": clean_text(category_title)
                    if category_title
                    else clean_text(header),
                    "links": links,
                }
            )
        menu.append(
            {
                "section": clean_text(title) if title else "",
                "categories": categories,
            }
        )
    return menu


def content_area(soup):
    return soup.select_one("#block-uae-content") or soup.select_one("main") or soup


def extract_page_links(content, page_url):
    links = []
    seen = set()
    for anchor in content.select("a[href]"):
        href = urljoin(page_url, anchor["href"])
        if urlparse(href).scheme not in {"http", "https"} or href in seen:
            continue
        seen.add(href)
        links.append({"title": clean_text(anchor), "url": href})
    return links


def fetch_page(session, url, verify_tls):
    try:
        response = session.get(
            url,
            timeout=REQUEST_TIMEOUT,
            verify=verify_tls,
        )
    except requests.RequestException as error:
        return {
            "url": url,
            "http_status": None,
            "error": str(error),
            "title": "",
            "text": "",
            "links": [],
            "soup": None,
        }

    soup = BeautifulSoup(response.content, "html.parser")
    title = clean_text(soup.title) if soup.title else ""
    content = content_area(soup)
    for element in content.select("script, style, nav, footer, .breadcrumb"):
        element.decompose()

    return {
        "url": response.url,
        "http_status": response.status_code,
        "error": None if response.ok else f"HTTP {response.status_code}",
        "title": title,
        "text": clean_text(content),
        "links": extract_page_links(content, response.url),
        "soup": soup,
    }


def extract_establishments(soup, page_url):
    establishments = []
    seen = set()
    for anchor in soup.select('a[href^="/etablissements/"]'):
        card = anchor.find_parent(class_="card")
        if card is None:
            continue

        profile_url = urljoin(page_url, anchor["href"])
        if profile_url in seen:
            continue
        seen.add(profile_url)

        name_element = card.select_one("h3.subtitle, h2, h3")
        city_element = card.select_one(".city-name")
        name = clean_text(name_element) if name_element else clean_text(anchor)
        city = clean_text(city_element) if city_element else None
        if city and re.search(r"\s(?:de|d['’])$", name, re.I):
            name = f"{name} {city}"
        establishments.append(
            {
                "name": name,
                "city": city,
                "profile_url": profile_url,
                "source_url": page_url,
                "statistics": [],
                "profile_http_status": None,
                "profile_error": None,
            }
        )
    return establishments


def relevant_document_links(pages):
    documents = []
    seen = set()
    for page in pages:
        for link in page["links"]:
            suffix = urlparse(link["url"]).path.lower()
            if not re.search(r"\.(pdf|docx?|xlsx?)$", suffix):
                continue
            if link["url"] in seen:
                continue
            seen.add(link["url"])
            title = link["title"].strip()
            if not title or re.fullmatch(
                r"(télécharger|download|visualiser|view)", title, re.I
            ):
                title = Path(suffix).stem.replace("-", " ").replace("_", " ").title()
            documents.append(
                {
                    "title": title,
                    "url": link["url"],
                    "source_page": page["url"],
                }
            )
    return documents


def find_evidence(text, keywords, source_url, limit=12):
    evidence = []
    seen = set()
    pattern = re.compile("|".join(re.escape(word) for word in keywords), re.IGNORECASE)
    for match in pattern.finditer(text):
        start = max(0, match.start() - 180)
        end = min(len(text), match.end() + 260)
        excerpt = re.sub(r"\s+", " ", text[start:end]).strip()
        if excerpt in seen:
            continue
        seen.add(excerpt)
        evidence.append({"excerpt": excerpt, "source_url": source_url})
        if len(evidence) >= limit:
            break
    return evidence


def scrape(args):
    session = requests.Session()
    session.headers.update({"User-Agent": USER_AGENT})
    verify_tls = not args.insecure
    if args.insecure:
        urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)
        warnings.warn(
            "TLS certificate verification is disabled by --insecure. "
            "Use this only if you trust the official site and understand the risk.",
            stacklevel=2,
        )

    page_results = {}
    errors = []
    for key, path in RESEARCH_PAGES.items():
        url = urljoin(BASE_URL, path)
        page = fetch_page(session, url, verify_tls)
        page_results[key] = page
        if page["error"]:
            errors.append(f"{url}: {page['error']}")
            print(f"Warning: could not fully fetch {url}: {page['error']}", file=sys.stderr)
        time.sleep(args.delay)

    press_pages = [page_results["press_releases"]]
    for page_number in range(1, args.press_pages):
        url = urljoin(BASE_URL, f"/communiques-presse?page={page_number}")
        page = fetch_page(session, url, verify_tls)
        press_pages.append(page)
        if page["error"]:
            errors.append(f"{url}: {page['error']}")
            print(f"Warning: could not fully fetch {url}: {page['error']}", file=sys.stderr)
        time.sleep(args.delay)

    index = page_results["establishments"]
    establishments = (
        extract_establishments(index["soup"], index["url"])
        if index["soup"] is not None
        else []
    )
    for establishment in establishments:
        profile = fetch_page(session, establishment["profile_url"], verify_tls)
        establishment["profile_http_status"] = profile["http_status"]
        if profile["error"]:
            establishment["profile_error"] = profile["error"]
            errors.append(f"{profile['url']}: {profile['error']}")
            print(
                f"Warning: could not fully fetch {profile['url']}: {profile['error']}",
                file=sys.stderr,
            )
        elif profile["soup"] is not None:
            establishment["statistics"] = extract_statistics(
                profile["soup"], profile["url"]
            )
        time.sleep(args.delay)

    pages = list(page_results.values()) + press_pages[1:]
    homepage = page_results["homepage"]
    homepage_statistics = (
        extract_statistics(homepage["soup"], homepage["url"])
        if homepage["soup"] is not None
        else []
    )
    documents = relevant_document_links(pages)
    enrollment_evidence = []
    for page in [page_results["homepage"], *press_pages]:
        enrollment_evidence.extend(
            find_evidence(
                page["text"],
                ["inscription", "nouveaux bacheliers", "rentrée", "étudiants inscrits"],
                page["url"],
            )
        )

    calendar = page_results["academic_calendar"]
    calendar_status = "content_available" if calendar["text"] else "no_content"
    if re.search(r"en cours de construction|under construction", calendar["text"], re.I):
        calendar_status = "under_construction"

    platform_links = []
    if homepage["soup"] is not None:
        platform_links = [
            link
            for section in extract_menu(homepage["soup"])
            for category in section["categories"]
            for link in category["links"]
            if re.search(r"moodle|plateforme pédagogique", link["title"], re.I)
        ]

    data = {
        "schema_version": 1,
        "scraped_at_utc": datetime.now(timezone.utc).isoformat(),
        "source_base_url": BASE_URL,
        "tls_verification_enabled": verify_tls,
        "university_statistics": homepage_statistics,
        "establishments": establishments,
        "menu_navigation": (
            extract_menu(homepage["soup"]) if homepage["soup"] is not None else []
        ),
        "background_pages": [
            {
                "title": page_results[key]["title"],
                "url": page_results[key]["url"],
                "text": page_results[key]["text"],
            }
            for key in ["history", "timeline"]
        ],
        "academic_calendar": {
            "url": calendar["url"],
            "status": calendar_status,
            "page_text": calendar["text"],
        },
        "digital_platform_evidence": {
            "menu_links": platform_links,
            "pedagogical_innovation_page": {
                "url": page_results["pedagogical_innovation"]["url"],
                "text": page_results["pedagogical_innovation"]["text"],
            },
            "deployment_model": None,
        },
        "digital_strategy_pages": [
            {
                "title": page_results[key]["title"],
                "url": page_results[key]["url"],
                "text": page_results[key]["text"],
            }
            for key in ["digital_strategy", "development_project"]
        ],
        "infrastructure_page": {
            "url": page_results["infrastructure"]["url"],
            "text": page_results["infrastructure"]["text"],
        },
        "enrollment_evidence": enrollment_evidence,
        "documents": documents,
        "page_status": [
            {
                "title": page["title"],
                "url": page["url"],
                "http_status": page["http_status"],
                "error": page["error"],
            }
            for page in pages
        ],
        "gaps": [
            "University-wide and establishment statistics have no publication/reference date on the source pages.",
            "Student/staff counts by establishment are included only when a profile publishes a numeric statistic; no value is inferred.",
            (
                "The academic-calendar page is under construction."
                if calendar_status == "under_construction"
                else "Check the academic-calendar page text and linked documents for registration/exam dates."
            ),
            "Registration/application volumes require confirmation from a relevant dated release; excerpts and official document links are provided as leads.",
            "The public pages do not establish whether Moodle is one central instance or one instance per establishment.",
            "The infrastructure page is not treated as evidence of server, datacenter, or network specifications.",
        ],
    }
    if not verify_tls:
        data["gaps"].append(
            "TLS certificate verification was disabled for this scrape; confirm critical values against the official site."
        )
    return data, errors


def markdown_report(data):
    lines = [
        "# UAE research scrape",
        "",
        f"Scraped: {data['scraped_at_utc']}",
        f"Official source: {data['source_base_url']}",
        f"TLS certificate verification: {'enabled' if data['tls_verification_enabled'] else 'disabled'}",
        "",
        "## University-wide figures",
        "",
        "| Value | Indicator | Source |",
        "| ---: | --- | --- |",
    ]
    for statistic in data["university_statistics"]:
        lines.append(
            f"| {statistic['value_display']} | {statistic['label']} | "
            f"[UAE homepage]({statistic['source_url']}) |"
        )
    if not data["university_statistics"]:
        lines.append("| — | No statistics extracted | — |")

    lines.extend(
        [
            "",
            "## Establishments",
            "",
            "| City | Establishment | Published numeric indicators | Profile |",
            "| --- | --- | --- | --- |",
        ]
    )
    for establishment in data["establishments"]:
        stats = "; ".join(
            f"{item['value_display']} {item['label']}"
            for item in establishment["statistics"]
        ) or "Not found"
        lines.append(
            f"| {establishment['city'] or 'Not stated'} | {establishment['name']} | "
            f"{stats} | [Official profile]({establishment['profile_url']}) |"
        )
    if not data["establishments"]:
        lines.append("| — | No establishment cards extracted | — | — |")

    calendar = data["academic_calendar"]
    lines.extend(
        [
            "",
            "## Academic calendar",
            "",
            f"Status: **{calendar['status']}**",
            "",
            calendar["page_text"] or "No page text was available.",
            "",
            f"Source: [{calendar['url']}]({calendar['url']})",
            "",
            "## Registration and enrollment leads",
            "",
        ]
    )
    for item in data["enrollment_evidence"]:
        lines.append(f"- “{item['excerpt']}” ([source]({item['source_url']}))")
    if not data["enrollment_evidence"]:
        lines.append("- No matching enrollment passage was found on the inspected home/press pages.")

    lines.extend(["", "## Digital strategy and services", ""])
    for page in data["digital_strategy_pages"]:
        lines.extend(
            [
                f"### {page['title'] or page['url']}",
                "",
                page["text"] or "No page text was available.",
                "",
                f"Source: [{page['url']}]({page['url']})",
                "",
            ]
        )
    lines.append(
        "Moodle menu links: "
        + (
            ", ".join(f"[{link['title']}]({link['url']})" for link in data["digital_platform_evidence"]["menu_links"])
            or "none found"
        )
    )
    innovation = data["digital_platform_evidence"]["pedagogical_innovation_page"]
    lines.extend(
        [
            "",
            f"Pedagogical innovation source: [{innovation['url']}]({innovation['url']})",
            "",
            innovation["text"] or "No page text was available.",
            "",
            "The public sources do not confirm a centralized or per-establishment Moodle deployment.",
        ]
    )

    lines.extend(
        [
            "",
            "## Infrastructure",
            "",
            data["infrastructure_page"]["text"] or "No page text was available.",
            "",
            f"Source: [{data['infrastructure_page']['url']}]({data['infrastructure_page']['url']})",
        ]
    )

    lines.extend(["", "## Official documents", ""])
    for document in data["documents"]:
        label = document["title"] or document["url"].rsplit("/", 1)[-1]
        lines.append(
            f"- [{label}]({document['url']}) — listed on "
            f"[source page]({document['source_page']})"
        )
    if not data["documents"]:
        lines.append("- No linked documents found on the inspected pages.")

    lines.extend(["", "## Data gaps and cautions", ""])
    lines.extend(f"- {gap}" for gap in data["gaps"])
    lines.extend(["", "## Inspected page status", ""])
    for page in data["page_status"]:
        status = page["http_status"] if page["http_status"] is not None else page["error"]
        lines.append(f"- [{page['title'] or page['url']}]({page['url']}) — {status}")
    return "\n".join(lines) + "\n"


def parse_args():
    parser = argparse.ArgumentParser(
        description="Scrape public UAE pages relevant to the CloudSim project."
    )
    parser.add_argument(
        "--output-dir",
        type=Path,
        default=DEFAULT_OUTPUT_DIR,
        help=f"Directory for uae_research.json and uae_research.md (default: {DEFAULT_OUTPUT_DIR})",
    )
    parser.add_argument(
        "--press-pages",
        type=int,
        default=PRESS_PAGE_COUNT,
        help="Number of press-release listing pages to inspect (default: 3)",
    )
    parser.add_argument(
        "--delay",
        type=float,
        default=0.25,
        help="Pause between establishment/press-page requests in seconds (default: 0.25)",
    )
    parser.add_argument(
        "--insecure",
        action="store_true",
        help="Disable TLS verification only if the official site has a broken certificate",
    )
    args = parser.parse_args()
    if args.press_pages < 1:
        parser.error("--press-pages must be at least 1")
    if args.delay < 0:
        parser.error("--delay cannot be negative")
    return args


def main():
    args = parse_args()
    data, errors = scrape(args)
    args.output_dir.mkdir(parents=True, exist_ok=True)
    json_path = args.output_dir / "uae_research.json"
    markdown_path = args.output_dir / "uae_research.md"
    json_path.write_text(
        json.dumps(data, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )
    markdown_path.write_text(markdown_report(data), encoding="utf-8")
    print(f"Wrote {json_path}")
    print(f"Wrote {markdown_path}")
    if errors:
        print(
            f"Completed with {len(errors)} page error(s); see page_status in the JSON.",
            file=sys.stderr,
        )
        return 1
    print(
        f"Collected {len(data['university_statistics'])} university figures, "
        f"{len(data['establishments'])} establishment profiles, and "
        f"{len(data['documents'])} document links."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())