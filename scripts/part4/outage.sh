#!/bin/bash
source ~/devstack/openrc admin admin 2>/dev/null
sleep 2400                                   # wait 40 minutes
echo "$(date '+%F %T') STOP  cirros-mon-2" | tee -a ~/part4/outage-events.log
openstack server stop cirros-mon-2
sleep 900                                    # outage lasts 15 minutes
echo "$(date '+%F %T') START cirros-mon-2" | tee -a ~/part4/outage-events.log
openstack server start cirros-mon-2
