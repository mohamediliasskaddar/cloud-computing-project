You've confirmed that the numbers on the site really are what it says, and that the site has inconsistencies. The logical approach is: **don't try to fix the site; build a model on defensible parameters, and document every assumption.** For a simulation we need only three things: the total number of users, how they split across sites, and how they behave at peak. The establishment-level mess only affects the second one, so it's much less serious than it looks.

## Step 1: Fix the headline total with a cross-check

| Indicator | 2022-2023 press release | uae.ac.ma now | Change |
|---|---|---|---|
| Students | 126,000 | 130,750 | +3.8% |
| Teacher-researchers | 1,318 | 1,357 | +3.0% |
| Administrative staff | 588 | 598 | +1.7% |

Two independent sources, three years apart, with modest and consistent growth. So **N = 130,750 is credible**, and we use it as the headline.

## Step 2: Test every establishment with a simple ratio

Students per teacher is a quick plausibility test. Most of the "box" rows land between 10 and 25.

| Case | Ratio | Verdict |
|---|---|---|
| FSTT 1,520 / 151 | 10 | plausible |
| ENSAT 1,290 / 58 | 22 | plausible |
| FS Tétouan 1,620 / 73 | 22 | plausible for a selective school, **doubtful for an open-access faculty** |
| FSJP Tanger 14,579 / 82 | 178 | plausible for open access (these have far larger intakes) |
| **ENCGT 27,269 / 65** | **419** | **impossible** |

## Step 3: A rule for each type of problem

| Problem | Rule |
|---|---|
| **ENCGT 27,269** | Read it as a **cumulative figure** (the box also says 25 years of existence and 4,407 graduates). That gives 27,269 / 25 ≈ **1,100 per year**, a ratio of 17:1, in line with the other schools. This is a hypothesis, so state it as one |
| **EST and ENS with identical figures** (and several schools sharing exactly 1,290 or 1,520) | The site probably reuses a template. Keep the values as **indicative only**, flagged "low confidence" |
| **FSJES Tanger and Tétouan** next to the newer FSJP and FEG | Likely legacy pages for the same programs. **Exclude them** to avoid double counting. This removes 2,220 students, under 3% of the total, so even a wrong guess changes little |
| **The duplicate FEG Tétouan page** ("Not found") | Drop the `/feg` entry and keep `/feg-tetouan` |

## Step 4: Use the establishment data only for the split between sites

I recomputed the shares after applying those rules. The detailed rows sum to about 78,000, against 130,750 on the homepage, so roughly 40% is unexplained. Some boxes probably aren't total enrollment. That's exactly why we **never use these rows as totals**.

| City | Share of the detailed rows |
|---|---|
| Tanger | 41.8% |
| Tétouan | 40.6% |
| Larache | 12.5% |
| Al Hoceima | 4.0% |
| Ksar El Kebir | 1.1% |

Attach each smaller city to the closer datacenter: Tanger side (Tanger, Larache, Ksar El Kebir) **about 55%**, Tétouan side (Tétouan, Al Hoceima) **about 45%**. Applied to N, that is roughly **72,000 users on the Tanger side and 59,000 on the Tétouan side**. Note that my earlier 44/38/16 split used only the open-access rows, so it was too tilted. The two main sites are nearly equal, which supports two datacenters.

## Step 5: Turn the weak data into a strength with a sensitivity analysis

Run the simulation on the baseline, then again with **N ±20%** and the site split shifted by **±10 points**. If the conclusion ("the architecture suffices at peak" or "it doesn't") holds across all those runs, it doesn't depend on the shaky numbers. That's a stronger argument than pretending the data was perfect.

## Step 6: Document it in the report

Add a short "Data quality" table: what the site publishes, the problem, the rule applied. It shows the examiner you validated the sources instead of copying them. I can generate that table, with a "value used" column, from your JSON.

## Next

If you agree with these rules, I'll write the fact sheet (with the headline numbers, the cleaned split and the data-quality table), then the architecture with sizing. Tell me if you want the JSON-based table first, or the architecture. Also say if you disagree with any rule, especially the cumulative reading of ENCGT, which is the one I'm least certain of.

Sources: [UAE press release, rentrée 2022-2023](https://www.uae.ma/website/sites/default/files/COMMUNIQUE%20DE%20PRESSE%20RENTREE%20UNIVERSITAIRE%20VF%282%29.pdf)