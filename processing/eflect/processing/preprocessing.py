import pandas as pd

# constants for vaporeon/jolteon experiments
WRAP_AROUND_VALUE = 16384
SAMPLE_INTERVAL = '50ms'
WINDOW_SIZE = '501ms'
# edit the check value (20) for system
DOMAIN_CONVERSION = lambda x: 0 if int(x) < 20 else 1

def bucket_timestamps(timestamps):
    return pd.to_datetime(timestamps).dt.floor(SAMPLE_INTERVAL)

def max_rolling_difference(df, window_size = WINDOW_SIZE):
    """ Computes a rolling difference of points up to the window size """
    values = df - df.rolling(window_size).min()

    timestamps = df.reset_index().timestamp.astype(int) / 10**9
    timestamps.index = df.index
    timestamps = timestamps - timestamps.rolling(window_size).min()

    return values, timestamps

def check_wrap_around(value):
    if value < 0:
        return value + WRAP_AROUND_VALUE
    else:
        return value

def process_energy_data(df):
    """ Computes the power of each 50ms bucket """
    df.columns = ['timestamp', 'domain', 'dram', 'cpu', 'package', 'gpu']
    df.timestamp = bucket_timestamps(df.timestamp)
    df = df.groupby(['timestamp', 'domain']).min()
    df.columns.name = 'component'

    energy, ts = max_rolling_difference(df.unstack())
    energy = energy.stack().stack().apply(check_wrap_around)
    energy = energy.groupby(['timestamp', 'domain']).sum().unstack().div(ts, axis = 0).stack()

    return energy

def process_app_data(df):
    """ Computes the app jiffy rate of each 50ms bucket """
    df.columns = ['timestamp', 'id', 'name', 'cpu', 'user', 'system']
    df['jiffies'] = df.user + df.system
    df = df[~df.name.str.contains('eflect-')]

    df.timestamp = bucket_timestamps(df.timestamp)
    df['domain'] = df.cpu.apply(DOMAIN_CONVERSION)
    df['id'] = df.id.astype(str) + '-' + df.name

    jiffies, ts = max_rolling_difference(df.groupby(['timestamp', 'id']).jiffies.min().unstack())
    jiffies = jiffies.stack().to_frame()
    domain = df.groupby(['timestamp', 'id']).domain.max()
    jiffies['domain'] = domain
    jiffies = jiffies.groupby(['timestamp', 'id', 'domain'])[0].sum().unstack().unstack().div(ts, axis = 0).stack().stack(0)

    return jiffies

def process_cpu_data(df):
    """ Computes the cpu jiffy rate of each 50ms bucket """
    df.columns = ['timestamp', 'cpu', 'user', 'nice', 'system', 'idle', 'iowait', 'irq', 'softirq', 'steal', 'guest', 'guest_nice']
    df['jiffies'] = df.drop(columns = ['timestamp', 'cpu', 'idle', 'iowait']).sum(axis = 1)
    df.timestamp = bucket_timestamps(df.timestamp)

    jiffies, ts = max_rolling_difference(df.groupby(['timestamp', 'cpu']).jiffies.min().unstack())
    jiffies = jiffies.stack().reset_index()
    jiffies['domain'] = jiffies.cpu.apply(DOMAIN_CONVERSION)
    jiffies = jiffies.groupby(['timestamp', 'domain']).sum().unstack()
    jiffies = jiffies.div(ts, axis = 0).stack()

    return jiffies.drop(columns = ['cpu'])[0]

def process_asyncprof_data(df):
    """ Formats the async-profiler data """
    df.columns = ['timestamp', 'id', 'trace']
    df.timestamp = bucket_timestamps(df.timestamp)
    return df.set_index(['timestamp', 'id']).sort_index().trace
