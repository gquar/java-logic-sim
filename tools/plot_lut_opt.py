import matplotlib
matplotlib.use("Agg")  # render to file, not a window

import matplotlib.pyplot as plt
import csv, math, sys, os

if len(sys.argv) != 3:
    print("Usage: python tools/plot_lut_opt.py <results.csv> <out.png>")
    sys.exit(1)

in_csv, out_png = sys.argv[1], sys.argv[2]
if not os.path.exists(in_csv):
    print(f"Error: CSV not found: {in_csv}")
    sys.exit(1)

sizes, mses, psnrs = [], [], []
with open(in_csv, newline='') as f:
    r = csv.DictReader(f)
    for row in r:
        try:
            sizes.append(int(row['size']))
            mses.append(float(row['mse']))
            val = row.get('psnr', 'nan').strip()
            ps = float('inf') if val.lower() in ('inf', 'infinity') else float(val)
            psnrs.append(ps)
        except Exception as ex:
            print("Skipping bad row:", row, ex)

# Cap infinities to keep the plot readable
cap = 60.0
psnrs_plot = [cap if math.isinf(p) else p for p in psnrs]

plt.figure(figsize=(6.5, 4.0))
plt.plot(sizes, psnrs_plot, marker='o')
plt.xlabel('LUT size (N)')
plt.ylabel('PSNR (dB)')
plt.title('LUT Size vs PSNR')
plt.grid(True, linestyle='--', alpha=0.4)
plt.tight_layout()
os.makedirs(os.path.dirname(out_png), exist_ok=True)
plt.savefig(out_png, dpi=200)
print(f"Wrote {out_png}")
