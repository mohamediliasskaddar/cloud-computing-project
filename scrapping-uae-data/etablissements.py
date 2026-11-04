from urllib.parse import urljoin
import json
import requests
from bs4 import BeautifulSoup
import urllib3

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

URL = "https://www.uae.ac.ma/etablissements"
BASE_URL = "https://www.uae.ac.ma"
HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
}

response = requests.get(URL, headers=HEADERS, verify=False)

if response.status_code == 200:
    soup = BeautifulSoup(response.content, "html.parser")

    # 1. Parsing du Menu
    mega_menu_items = soup.select("ul.navbar-nav > li.mega-dropdown")
    extracted_menu = []

    for item in mega_menu_items:
        section_title = item.select_one("a.nav-link")
        section_name = (
            section_title.get_text(strip=True) if section_title else "N/A"
        )

        categories = []
        for header in item.select("li.dropdown-header"):
            header_title = header.select_one("span")
            cat_name = (
                header_title.get_text(strip=True) if header_title else "N/A"
            )

            links = []
            for a_tag in header.select("ul li a"):
                link_text = a_tag.get_text(strip=True)
                link_href = a_tag.get("href", "")
                full_url = urljoin(BASE_URL, link_href)
                links.append({"titre": link_text, "url": full_url})

            categories.append({"sous_categorie": cat_name, "liens": links})

        extracted_menu.append(
            {"section_principale": section_name, "categories": categories}
        )

    # 2. Parsing des Établissements (Extraction propre des blocs/cartes)
    etablissements_list = []
    # On cherche les blocs individuels des établissements plutôt que le texte brut
    cards = soup.select(
        ".view-etablissements .views-row, .page-etablissements .card, .node--type-etablissement"
    )

    if cards:
        for card in cards:
            title = card.select_one("h2, h3, .field--name-title")
            city = card.select_one(".field--name-field-ville, .ville")
            if title:
                etablissements_list.append(
                    {
                        "nom": title.get_text(strip=True),
                        "ville": (
                            city.get_text(strip=True) if city else "Non précisée"
                        ),
                    }
                )

    data = {"menu_navigation": extracted_menu, "etablissements": etablissements_list}

    # --- SAUVEGARDE EN JSON ---
    with open("data/uae_data.json", "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=4)
    print("✓ Fichier data/uae_data.json généré avec succès !")

    # --- SAUVEGARDE EN MARKDOWN ---
    md_content = "# Structure et Établissements - UAE\n\n"
    md_content += "## 1. Menu de Navigation\n\n"

    for section in extracted_menu:
        md_content += f"### {section['section_principale']}\n"
        for cat in section["categories"]:
            md_content += f"#### {cat['sous_categorie']}\n"
            for link in cat["liens"]:
                md_content += f"- [{link['titre']}]({link['url']})\n"
            md_content += "\n"

    if etablissements_list:
        md_content += "## 2. Établissements\n\n"
        md_content += "| Ville | Nom de l'Établissement |\n"
        md_content += "| --- | --- |\n"
        for etab in etablissements_list:
            md_content += f"| {etab['ville']} | {etab['nom']} |\n"

    with open("data/uae_data.md", "w", encoding="utf-8") as f:
        f.write(md_content)
    print("✓ Fichier data/uae_data.md généré avec succès !")

else:
    print(f"Erreur HTTP : {response.status_code}")