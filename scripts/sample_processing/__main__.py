import os

from sys import argv

import matplotlib.pyplot as plt
import numpy as np

from evaluation import *
from processing import pre_process

def main():
    root = argv[1]

    print("processing sample data at \"" + root + "\"")
    app, cpu, energy, traces = pre_process(root)

    accounted = account_application_energy(app, cpu, energy)
    accounted.to_csv(os.path.join(root, 'accounted-energy.csv'))

    app = accounted.groupby(['timestamp', 'domain']).sum()
    print('app consumed ' + '{:.2f}'.format(app.sum()) + '/' + '{:.2f}'.format(energy.sum()) + ' J')
    fig, axs = energy_accounting_plot(app.unstack().rolling('50ms').mean().stack(), energy.unstack().rolling('50ms').mean().stack())
    plt.savefig(os.path.join(root, 'accounted-energy.pdf'), bbox_inches = 'tight')
    plt.close()

    threads = accounted.reset_index()
    threads.id = threads.id.str.split('-').str[0].fillna(-1).astype(int)
    threads = threads.set_index(['timestamp', 'id'])[0]

    traces = traces.set_index(['timestamp', 'id'])

    aligned = pd.merge(threads, traces, left_index = True, right_index = True)
    aligned.columns = ['energy', 'trace']
    aligned.energy = aligned.energy / aligned.groupby(['timestamp', 'id']).energy.count()

    aligned.to_csv(os.path.join(root, 'aligned-energy.csv'))

    return

if __name__ == '__main__':
    main()
