```
ssh imk@192.168.56.101
```
```
sudo -u stack -i
```
```
cd devstack
```


sudo apt update
sudo apt install -y python3-flask
mkdir -p ~/app && cd ~/app
cat > app.py <<'EOF'
from flask import Flask
import socket
app = Flask(__name__)

@app.route("/")
def home():
    return f"<h1>Hello from my OpenStack SaaS app</h1><p>Mohamed Iliass KADDAR - Cloud Intelligence project</p><p>Served by instance: {socket.gethostname()}</p>"

app.run(host="0.0.0.0", port=5000)
EOF
python3 app.py









cat > app.py <<'EOF'
from flask import Flask
import socket
app = Flask(__name__)

@app.route("/")
def home():
    return f"<h1>Hello from my OpenStack SaaS app</h1><p>Mohamed Iliass KADDAR - Cloud Intelligence project</p><p>Served by instance: {socket.gethostname()}</p>"

app.run(host="0.0.0.0", port=5000)
EOF