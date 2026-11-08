# UAE research scraper

`scrapper.py` collects official UAE pages useful for Part 1 of the CloudSim
project: university-wide homepage figures, establishment names and cities,
numeric figures published on establishment profiles, academic-calendar status,
press-release evidence and document links, digital strategy, and public
infrastructure information.

From the project root, create the environment, install dependencies, and run:

```bash
python3 -m venv scrapping-uae-data/.venv
scrapping-uae-data/.venv/bin/python -m pip install -r scrapping-uae-data/requirements.txt
scrapping-uae-data/.venv/bin/python scrapping-uae-data/scrapper.py
```

By default, the script writes `data/uae_research.json` and
`data/uae_research.md` next to the script. It does not overwrite the existing
`data/uae_data.json` or `data/uae_data.md`.

The script verifies HTTPS certificates by default. If the official site's
certificate cannot be verified in your environment, you can explicitly opt
out for that run:

```bash
./.venv/bin/python scrapper.py --insecure
```

Use `--help` to see output-directory, press-page, and request-delay options.
Facts without publication dates remain undated, and missing per-establishment
counts, calendar dates, and Moodle deployment details are reported as gaps
rather than guessed.
