#!/bin/bash

SWAPFILE="/swapfile"
SIZE=2G

echo ">> Creating swap file: $SWAPFILE with size $SIZE"

if [ ! -f "$SWAPFILE" ]; then
  sudo fallocate -l $SIZE $SWAPFILE
  sudo chmod 600 $SWAPFILE
  sudo mkswap $SWAPFILE
  sudo swapon $SWAPFILE
  echo "$SWAPFILE none swap sw 0 0" | sudo tee -a /etc/fstab
  echo ">> Swap successfully created and activated."
else
  echo ">> Swap file already exists. Skipping creation."
fi