import os

from sys import argv

import matplotlib.pyplot as plt
import numpy as np

from evaluation import *
from processing import pre_process

def main():
    root = argv[1]
    print(root)

    # app, cpu, energy = pre_process(root)
    #
    # # hacky code
    # fig, axs = jiffies_plot(app.unstack().unstack().rolling('5s').mean().stack().stack().groupby(['timestamp', 'run', 'domain']).sum(), cpu.unstack().unstack().rolling('5s').mean().stack().stack().groupby(['timestamp', 'run', 'domain']).sum())
    #
    # name = root.split(os.path.sep)[-1]
    # plt.suptitle(name, fontsize = 24)
    # plt.savefig('plots/' + name + '-sys-jiffies.pdf', bbox_inches = 'tight')
    # plt.close()

    df = account_jiffies(*pre_process(root))
    print(df)
    df = df.unstack().unstack().dropna().stack().stack()
    df = df.groupby(['timestamp', 'run', 'domain']).agg({'app': 'sum', 'total': 'mean'})

    app = df.app.unstack().unstack().rolling('5s').mean().stack().stack().groupby(['timestamp', 'run', 'domain']).sum()
    total = df.total.unstack().unstack().rolling('5s').mean().stack().stack().groupby(['timestamp', 'run', 'domain']).mean()

    metrics = compute_metrics(app.groupby(['timestamp', 'domain']).sum(), total.groupby(['timestamp', 'domain']).mean())
    fig, axs = jiffies_accounting_plot(app, total, metrics)

    name = root.split(os.path.sep)[-1]
    plt.suptitle(name, fontsize = 24)
    plt.savefig('plots/' + name + '-jiffies.pdf', bbox_inches = 'tight')
    plt.close()

    df = account_application_energy(*pre_process(root))
    df = df.unstack().unstack().dropna().stack().stack()
    df = df.groupby(['timestamp', 'run', 'domain']).agg({'app': 'sum', 'total': 'mean'})

    app = df.app.unstack().unstack().rolling('5s').mean().stack().stack().groupby(['timestamp', 'run', 'domain']).sum()
    total = df.total.unstack().unstack().rolling('5s').mean().stack().stack().groupby(['timestamp', 'run', 'domain']).mean()

    metrics = compute_metrics(app.groupby(['timestamp', 'domain']).sum(), total.groupby(['timestamp', 'domain']).mean())
    fig, axs = energy_accounting_plot(app, total, metrics)

    name = root.split(os.path.sep)[-1]
    plt.suptitle(name, fontsize = 24)
    plt.savefig('plots/' + name + '.pdf', bbox_inches = 'tight')
    plt.close()

if __name__ == '__main__':
    main()
