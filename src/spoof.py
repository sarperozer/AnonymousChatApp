import sys
from scapy.all import *
from scapy.layers.inet import *

src_mac, src_ip, dst_mac, dst_ip, port = sys.argv[1:6]
payload = " ".join(sys.argv[6:])

packet = (
    Ether(src=src_mac, dst=dst_mac) /
    IP(src=src_ip,   dst=dst_ip)   /
    UDP(sport=int(port), dport=int(port)) /
    payload
)

sendp(packet)