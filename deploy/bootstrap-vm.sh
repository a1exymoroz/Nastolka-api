#!/usr/bin/env bash
# One-time setup for the Oracle Cloud Always Free VM (Ubuntu 22.04/24.04).
# Run once over SSH after the VM is provisioned and DNS points at it:
#   scp -r deploy ubuntu@<VM_HOST>:~/nastolka-deploy
#   ssh ubuntu@<VM_HOST>
#   sudo bash ~/nastolka-deploy/bootstrap-vm.sh
set -euo pipefail

DEPLOY_DIR=/opt/nastolka-api
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "Installing Docker Engine + Compose plugin..."
apt-get update -y
apt-get install -y ca-certificates curl gnupg
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
chmod a+r /etc/apt/keyrings/docker.asc
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
apt-get update -y
apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

echo "Allowing ${SUDO_USER:-$USER} to run docker without sudo..."
usermod -aG docker "${SUDO_USER:-$USER}"

echo "Opening local firewall for HTTP/HTTPS (OCI Ubuntu images block these by default)..."
apt-get install -y iptables-persistent
iptables -I INPUT -p tcp --dport 80 -j ACCEPT
iptables -I INPUT -p tcp --dport 443 -j ACCEPT
netfilter-persistent save

echo "Setting up ${DEPLOY_DIR}..."
mkdir -p "${DEPLOY_DIR}"
cp "${SCRIPT_DIR}/docker-compose.yml" "${DEPLOY_DIR}/"
cp "${SCRIPT_DIR}/Caddyfile" "${DEPLOY_DIR}/"

if [ ! -f "${DEPLOY_DIR}/.env" ]; then
  echo
  echo "No .env found at ${DEPLOY_DIR}/.env — create it before starting the stack."
  echo "See README.md 'Deployment' section for the required variables."
fi

cat <<EOF

Bootstrap done. Next steps:
  1. Create ${DEPLOY_DIR}/.env with the app's runtime secrets (see README.md)
  2. Edit ${DEPLOY_DIR}/Caddyfile if your DuckDNS hostname differs from the placeholder
  3. Log out/in (or run 'newgrp docker') so the docker group membership takes effect
  4. cd ${DEPLOY_DIR} && docker compose up -d
EOF
