terraform {
  required_providers {
    openstack = {
      source  = "terraform-provider-openstack/openstack"
      version = ">= 3.0.0"
    }
  }
}

# Credentials are read from /etc/openstack/clouds.yaml
provider "openstack" {
  cloud = "devstack-admin"
}

# Existing private network
data "openstack_networking_network_v2" "private" {
  name = var.network_name
}

# Security group: SSH + HTTP
resource "openstack_networking_secgroup_v2" "web" {
  name        = "sg-terraform"
  description = "SSH and HTTP (managed by Terraform)"
}

resource "openstack_networking_secgroup_rule_v2" "ssh" {
  direction         = "ingress"
  ethertype         = "IPv4"
  protocol          = "tcp"
  port_range_min    = 22
  port_range_max    = 22
  remote_ip_prefix  = "0.0.0.0/0"
  security_group_id = openstack_networking_secgroup_v2.web.id
}

resource "openstack_networking_secgroup_rule_v2" "http" {
  direction         = "ingress"
  ethertype         = "IPv4"
  protocol          = "tcp"
  port_range_min    = 80
  port_range_max    = 80
  remote_ip_prefix  = "0.0.0.0/0"
  security_group_id = openstack_networking_secgroup_v2.web.id
}

# Network port carrying the security group
resource "openstack_networking_port_v2" "vm" {
  name               = "${var.instance_name}-port"
  network_id         = data.openstack_networking_network_v2.private.id
  security_group_ids = [openstack_networking_secgroup_v2.web.id]
  admin_state_up     = true
}

# Ubuntu instance
resource "openstack_compute_instance_v2" "vm" {
  name        = var.instance_name
  image_name  = var.image_name
  flavor_name = var.flavor_name
  key_pair    = var.key_pair

  network {
    port = openstack_networking_port_v2.vm.id
  }
}

# Floating IP + association
resource "openstack_networking_floatingip_v2" "fip" {
  pool = var.external_pool
}

resource "openstack_networking_floatingip_associate_v2" "fip_assoc" {
  floating_ip = openstack_networking_floatingip_v2.fip.address
  port_id     = openstack_networking_port_v2.vm.id
}
