## Step 1: Fix the headline total with a cross-check

| Indicator | 2022-2023 press release | uae.ac.ma now | Change |
|---|---|---|---|
| Students | 126,000 | 130,750 | +3.8% |
| Teacher-researchers | 1,318 | 1,357 | +3.0% |
| Administrative staff | 588 | 598 | +1.7% |

Two independent sources, three years apart, show modest and consistent growth. Use **130,750 as the published student count**, not as the total platform-user population. For this simulation, include the two published staff categories too: 130,750 students + 1,357 teacher-researchers + 598 administrative staff = **132,705 potential users**. This is still an estimate (not a measured Moodle account count or active-user count). The separate 5% normal and 30% peak concurrency rates are workload assumptions, not facts reported by the university.

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

Attach each smaller city to the closer datacenter: Tanger side (Tanger, Larache, Ksar El Kebir) **about 55%**, Tétouan side (Tétouan, Al Hoceima) **about 45%**. Applied to the current estimated total of 132,705 potential users, that is roughly **73,000 users on the Tanger side and 60,000 on the Tétouan side**. Note that my earlier 44/38/16 split used only the open-access rows, so it was too tilted. The two main sites are nearly equal, which supports two datacenters.

## Step 5: Sensitivity analysis still needed

The current four-run benchmark does **not** vary population or site split; those runs only compare scheduling and autoscaling under one synthetic workload. A future sensitivity analysis could vary N by ±20% and the site split by ±10 percentage points, after a defensible mapping from users to requests has been measured. Until then, the current benchmark cannot establish whether the architecture is sufficient for the actual peak population.

## Step 6: Use the data carefully in the report

State separately the published student count (130,750), the estimated potential-user total including staff (132,705), and the assumed active-user concurrency. Preserve the unknown reference date and scraping TLS caveat. The code and detailed modeling choices are documented in [`../part1-cloudsim/guide.md`](../part1-cloudsim/guide.md); do not describe the synthetic 50/300 Cloudlets as production requests per second.

Sources: [UAE press release, rentrée 2022-2023](https://www.uae.ma/website/sites/default/files/COMMUNIQUE%20DE%20PRESSE%20RENTREE%20UNIVERSITAIRE%20VF%282%29.pdf)