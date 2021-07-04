import os

from sys import argv

import numpy as np
import pandas as pd

from eflect.processing.preprocessing import process_app_data
from eflect.processing.preprocessing import process_cpu_data
from eflect.processing.preprocessing import process_energy_data
from eflect.processing.preprocessing import process_yappi_data

def account_application_energy(app, cpu, energy):
    return energy * (app / cpu).replace(np.inf, 1).clip(0, 1)

def pre_process(data_dir):
    app = []
    cpu = []
    energy = []
    traces = []
    for f in os.listdir(os.path.join(data_dir)):
        df = pd.read_csv(os.path.join(data_dir, f))
        if 'ProcTaskSample' in f:
            app.append(process_app_data(df))
        elif 'ProcStatSample' in f:
            cpu.append(process_cpu_data(df))
        elif 'EnergySample' in f:
            energy.append(process_energy_data(df))

    return pd.concat(app), pd.concat(cpu), pd.concat(energy)

def align_methods(footprints, data_dir):
        energy = []
        for f in os.listdir(os.path.join(data_dir)):
            df = pd.read_csv(os.path.join(data_dir, f))
            if 'YappiSample' in f:
                df = footprints.groupby('id').sum() * process_yappi_data(df)
                df = df.groupby('trace').sum().sort_values(ascending=False)
                energy.append(df)

        return pd.concat(energy)

def account_energy(path):
    app, cpu, energy = pre_process(path)

    footprint = account_application_energy(app, cpu, energy).dropna().reset_index()
    footprint = footprint.assign(id = footprint.id.str.split('-').str[0].astype(int)).set_index(['timestamp', 'id'])[0]
    footprint.name = 'energy'

    ranking = align_methods(footprint, path)
    ranking.name = 'energy'
    ranking = ranking / ranking.sum()

    return footprint, ranking
