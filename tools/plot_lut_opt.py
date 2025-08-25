#!/usr/bin/env python3
"""
Plot LUT optimization results from CSV file.

Usage:
    python3 tools/plot_lut_opt.py input.csv output.png
"""

import sys

# Try to import matplotlib, numpy, and pandas, fallback to csv if not available
try:
    import matplotlib.pyplot as plt
    USE_MATPLOTLIB = True
except ImportError:
    USE_MATPLOTLIB = False

try:
    import numpy as np
    USE_NUMPY = True
except ImportError:
    USE_NUMPY = False

try:
    import pandas as pd
    USE_PANDAS = True
except ImportError:
    import csv
    import math
    USE_PANDAS = False

def plot_lut_optimization(csv_path, output_path):
    """Plot LUT size vs PSNR and MSE from CSV data."""
    
    if not USE_MATPLOTLIB or not USE_NUMPY:
        print("matplotlib or numpy not available, cannot generate plot")
        print("Install dependencies: pip install matplotlib numpy")
        return
    
    # Read CSV data
    if USE_PANDAS:
        try:
            df = pd.read_csv(csv_path)
        except FileNotFoundError:
            print(f"Error: Could not find CSV file '{csv_path}'")
            sys.exit(1)
        except Exception as e:
            print(f"Error reading CSV: {e}")
            sys.exit(1)
    else:
        # Fallback to csv module
        try:
            sizes, train_mses, train_psnrs, val_mses, val_psnrs = [], [], [], [], []
            has_validation = False
            
            with open(csv_path, newline='') as f:
                reader = csv.DictReader(f)
                for row in reader:
                    sizes.append(int(row['size']))
                    train_mses.append(float(row['train_mse']))
                    
                    # Handle PSNR (may be 'inf')
                    train_psnr_str = row['train_psnr'].strip()
                    if train_psnr_str.lower() == 'inf':
                        train_psnrs.append(float('inf'))
                    else:
                        train_psnrs.append(float(train_psnr_str))
                    
                    # Check for validation data
                    if 'val_psnr' in row and row['val_psnr'].strip():
                        has_validation = True
                        val_mses.append(float(row['val_mse']))
                        val_psnr_str = row['val_psnr'].strip()
                        if val_psnr_str.lower() == 'inf':
                            val_psnrs.append(float('inf'))
                        else:
                            val_psnrs.append(float(val_psnr_str))
                    else:
                        val_mses.append(0.0)
                        val_psnrs.append(0.0)
            
            # Create a simple dict-like object for compatibility
            class SimpleDF:
                def __init__(self, data):
                    self.data = data
                    self.columns = list(data.keys())
                
                def __getitem__(self, key):
                    return self.data[key]
                
                def __len__(self):
                    return len(self.data[self.columns[0]])
                
                def iterrows(self):
                    for i in range(len(self)):
                        yield i, {col: self.data[col][i] for col in self.columns}
            
            df = SimpleDF({
                'size': sizes,
                'train_mse': train_mses,
                'train_psnr': train_psnrs,
                'val_mse': val_mses,
                'val_psnr': val_psnrs
            })
            
        except FileNotFoundError:
            print(f"Error: Could not find CSV file '{csv_path}'")
            sys.exit(1)
        except Exception as e:
            print(f"Error reading CSV: {e}")
            sys.exit(1)
    
    # Validate required columns
    required_cols = ['size', 'train_psnr']
    missing_cols = [col for col in required_cols if col not in df.columns]
    if missing_cols:
        print(f"Error: Missing required columns: {missing_cols}")
        sys.exit(1)
    
    # Check if validation data is available
    if USE_PANDAS:
        has_validation = 'val_psnr' in df.columns and not df['val_psnr'].isna().all()
    else:
        has_validation = has_validation  # From fallback parsing
    
    # Create figure with two subplots
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(12, 5))
    
    # Plot 1: Size vs PSNR
    # Handle infinity values for training PSNR
    if USE_PANDAS:
        train_psnr = df['train_psnr'].replace([np.inf, -np.inf], 65.0)
    else:
        train_psnr = [65.0 if math.isinf(p) else p for p in df['train_psnr']]
    ax1.plot(df['size'], train_psnr, 'bo-', linewidth=2, markersize=8, label='Train PSNR')
    
    # Add validation PSNR if available
    if has_validation:
        if USE_PANDAS:
            val_psnr = df['val_psnr'].replace([np.inf, -np.inf], 65.0)
        else:
            val_psnr = [65.0 if math.isinf(p) else p for p in df['val_psnr']]
        ax1.plot(df['size'], val_psnr, 'rs-', linewidth=2, markersize=8, label='Val PSNR')
        ax1.legend()
    
    ax1.set_xlabel('LUT Size')
    ax1.set_ylabel('PSNR (dB)')
    ax1.set_title('LUT Size vs PSNR')
    ax1.grid(True, alpha=0.3)
    
    # Add value labels on points (handle infinity)
    for i, row in df.iterrows():
        psnr_val = row['train_psnr']
        if USE_PANDAS:
            is_inf = np.isinf(psnr_val)
        else:
            is_inf = math.isinf(psnr_val)
        
        if is_inf:
            ax1.annotate("∞", 
                        (row['size'], 65.0), 
                        textcoords="offset points", 
                        xytext=(0,10), 
                        ha='center', fontsize=12, weight='bold')
        else:
            ax1.annotate(f"{psnr_val:.1f}", 
                        (row['size'], psnr_val), 
                        textcoords="offset points", 
                        xytext=(0,10), 
                        ha='center', fontsize=9)
    
    # Plot 2: Size vs MSE
    ax2.plot(df['size'], df['train_mse'], 'bo-', linewidth=2, markersize=8, label='Train MSE')
    
    if has_validation:
        ax2.plot(df['size'], df['val_mse'], 'rs-', linewidth=2, markersize=8, label='Val MSE')
        ax2.legend()
    
    ax2.set_xlabel('LUT Size')
    ax2.set_ylabel('MSE')
    ax2.set_title('LUT Size vs MSE')
    ax2.grid(True, alpha=0.3)
    
    # Add value labels on points
    for i, row in df.iterrows():
        ax2.annotate(f"{row['train_mse']:.4f}", 
                    (row['size'], row['train_mse']), 
                    textcoords="offset points", 
                    xytext=(0,10), 
                    ha='center', fontsize=9)
    
    # Adjust layout and save
    plt.tight_layout()
    
    try:
        plt.savefig(output_path, dpi=150, bbox_inches='tight')
        print(f"Plot saved to: {output_path}")
    except Exception as e:
        print(f"Error saving plot: {e}")
        sys.exit(1)
    
    # Print summary
    print("\nOptimization Summary:")
    if USE_PANDAS:
        train_psnr_max = df['train_psnr'].replace([np.inf, -np.inf], np.nan).max()
        if not np.isnan(train_psnr_max):
            print(f"Best Train PSNR: {train_psnr_max:.2f} dB (size {df.loc[df['train_psnr'].replace([np.inf, -np.inf], np.nan).idxmax(), 'size']})")
        else:
            print("Best Train PSNR: ∞ dB (perfect reconstruction)")
        
        print(f"Lowest Train MSE: {df['train_mse'].min():.6f} (size {df.loc[df['train_mse'].idxmin(), 'size']})")
        
        if has_validation:
            val_psnr_max = df['val_psnr'].replace([np.inf, -np.inf], np.nan).max()
            if not np.isnan(val_psnr_max):
                print(f"Best Val PSNR: {val_psnr_max:.2f} dB (size {df.loc[df['val_psnr'].replace([np.inf, -np.inf], np.nan).idxmax(), 'size']})")
            else:
                print("Best Val PSNR: ∞ dB (perfect reconstruction)")
        
        # Find smallest size meeting common PSNR goals
        goals = [30, 36, 40]
        for goal in goals:
            meeting_goal = df[df['train_psnr'] >= goal]
            if not meeting_goal.empty:
                smallest = meeting_goal.loc[meeting_goal['size'].idxmin()]
                psnr_val = smallest['train_psnr']
                if np.isinf(psnr_val):
                    print(f"Smallest size for {goal} dB train: {smallest['size']} (PSNR: ∞ dB)")
                else:
                    print(f"Smallest size for {goal} dB train: {smallest['size']} (PSNR: {psnr_val:.2f} dB)")
    else:
        # Simple fallback summary
        train_psnrs = df['train_psnr']
        train_mses = df['train_mse']
        sizes = df['size']
        
        max_idx = 0
        for i, psnr in enumerate(train_psnrs):
            if not math.isinf(psnr) and (math.isinf(train_psnrs[max_idx]) or psnr > train_psnrs[max_idx]):
                max_idx = i
        
        if math.isinf(train_psnrs[max_idx]):
            print("Best Train PSNR: ∞ dB (perfect reconstruction)")
        else:
            print(f"Best Train PSNR: {train_psnrs[max_idx]:.2f} dB (size {sizes[max_idx]})")
        
        min_mse_idx = train_mses.index(min(train_mses))
        print(f"Lowest Train MSE: {train_mses[min_mse_idx]:.6f} (size {sizes[min_mse_idx]})")

def main():
    if len(sys.argv) != 3:
        print("Usage: python3 tools/plot_lut_opt.py input.csv output.png")
        sys.exit(1)
    
    csv_path = sys.argv[1]
    output_path = sys.argv[2]
    
    plot_lut_optimization(csv_path, output_path)

if __name__ == "__main__":
    main()
