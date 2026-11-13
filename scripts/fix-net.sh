#!/bin/bash
# Restore DevStack floating-IP networking after a reboot
sudo ip addr replace 172.24.4.1/24 dev br-ex
sudo ip link set br-ex up
sudo sysctl -w net.ipv4.ip_forward=1
IFACE=$(ip route | awk '/default/ {print $5; exit}')
sudo iptables -t nat -C POSTROUTING -s 172.24.4.0/24 -o "$IFACE" -j MASQUERADE 2>/dev/null \
  || sudo iptables -t nat -A POSTROUTING -s 172.24.4.0/24 -o "$IFACE" -j MASQUERADE
echo "Done. br-ex:"; ip -br a show br-ex
