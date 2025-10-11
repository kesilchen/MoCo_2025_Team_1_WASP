import re
import sys
from datetime import datetime
import pandas as pd
import matplotlib.pyplot as plt


def parse_timestamp(text: str):
    for pattern in ("%Y-%m-%d %H:%M:%S.%f", "%Y-%m-%d %H:%M:%S"):
        try:
            return datetime.strptime(text, pattern)
        except ValueError:
            continue
    return None


def to_float_or_nan(value: str):
    try:
        return float(value)
    except Exception:
        return float("nan")


def parse_old_logs(path: str, board_id="01:01"):
    records = []
    pattern = re.compile(
        r"(\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2}:\d{2}(?:\.\d+)?).*?\b([0-9]{2}:[0-9]{2}):\s+([^\s]+)\s+([^\s]+)\s+([^\s]+)"
    )
    with open(path, "r", encoding="utf-8", errors="ignore") as file:
        for line in file:
            match = pattern.search(line)
            if not match:
                continue
            timestamp = parse_timestamp(match.group(1))
            board = match.group(2)
            if board != board_id:
                continue
            distance, azimuth, elevation = match.group(3), match.group(4), match.group(5)
            records.append({
                "timestamp": timestamp,
                "distance_old": to_float_or_nan(distance),
                "azimuth_old": to_float_or_nan(azimuth),
                "elevation_old": to_float_or_nan(elevation),
            })
    df = pd.DataFrame(records)
    return df.dropna(subset=["timestamp"]).sort_values("timestamp").reset_index(drop=True)


def parse_new_logs(path: str):
    records = []
    timestamp_pattern = re.compile(r"(\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2}:\d{2}(?:\.\d+)?)")
    number_pattern = re.compile(r"([+\-]?\d+(?:\.\d+)?(?:[eE][+\-]?\d+)?)")
    with open(path, "r", encoding="utf-8", errors="ignore") as file:
        for line in file:
            timestamps = timestamp_pattern.findall(line)
            if not timestamps:
                continue
            timestamp = parse_timestamp(timestamps[-1])
            if timestamp is None:
                continue
            numbers = number_pattern.findall(line)
            if len(numbers) < 3:
                continue
            distance, azimuth, elevation = numbers[-3], numbers[-2], numbers[-1]
            records.append({
                "timestamp": timestamp,
                "distance_new": to_float_or_nan(distance),
                "azimuth_new": to_float_or_nan(azimuth),
                "elevation_new": to_float_or_nan(elevation),
            })
    df = pd.DataFrame(records)
    return df.dropna(subset=["timestamp"]).sort_values("timestamp").reset_index(drop=True)


def merge_logs(old_df: pd.DataFrame, new_df: pd.DataFrame):
    return pd.merge(old_df, new_df, on="timestamp", how="outer").sort_values("timestamp").reset_index(drop=True)


def plot_uwb_data(merged: pd.DataFrame):
    plt.figure()
    plt.plot(merged["timestamp"], merged["distance_new"], label="new logs")
    plt.plot(merged["timestamp"], merged["distance_old"], label="old logs 01:01")
    plt.xlabel("Time")
    plt.ylabel("Distance [m]")
    plt.title("Distance")
    plt.legend()
    plt.tight_layout()

    plt.figure()
    plt.plot(merged["timestamp"], merged["azimuth_new"], label="new logs")
    plt.plot(merged["timestamp"], merged["azimuth_old"], label="old logs 01:01")
    plt.xlabel("Time")
    plt.ylabel("Azimuth [°]")
    plt.title("Azimuth")
    plt.legend()
    plt.tight_layout()

    plt.figure()
    plt.plot(merged["timestamp"], merged["elevation_new"], label="new logs")
    plt.plot(merged["timestamp"], merged["elevation_old"], label="old logs 01:01")
    plt.xlabel("Time")
    plt.ylabel("Elevation [°]")
    plt.title("Elevation")
    plt.legend()
    plt.tight_layout()

    plt.show()


def main(old_log_path: str, new_log_path: str):
    old_df = parse_old_logs(old_log_path, board_id="01:01")
    new_df = parse_new_logs(new_log_path)
    merged_df = merge_logs(old_df, new_df)
    if merged_df.empty:
        print("No matching data found.")
        return
    plot_uwb_data(merged_df)


if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python plot_uwb.py old_logs.txt new_logs.txt")
        sys.exit(1)
    main(sys.argv[1], sys.argv[2])