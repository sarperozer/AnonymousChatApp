import sys
from scapy.all import *
from scapy.layers.inet import *

src_mac, src_ip, dst_mac, dst_ip, port = sys.argv[1:6]
body = " ".join(sys.argv[6:])

packet = (
    IP(src=src_ip,   dst=dst_ip)   /
    UDP(sport=int(port), dport=int(port)) /
    body
)

send(packet)