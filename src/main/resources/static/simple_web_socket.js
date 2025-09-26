class RPASocket {

    constructor(button, base_path = this.getBasePath()) {
        this.button = button;
        this.base_path = base_path;
    }

    init() {
        this.button.addEventListener('click', () => { this.startProcess() })
    }

    startProcess() {
        this.simpleWebSocket = new WebSocket(`wss:server:port/dwpwebsocket/wsDwpHandler`);

        this.simpleWebSocket.onopen = () => {
            this.simpleWebSocket.send(JSON.stringify({ type: "START", session: 987654321 }));
        }

        this.simpleWebSocket.onmessage = (event) => {
            console.log(`Received Message ${event.data}`);
            const message = JSON.parse(event.data)
            switch (message.type) {
                case "LOGIN":
                    // Go to the apex page
                    // Register a websocket on the button
                    this.simpleWebSocket.send(JSON.stringify({ type: "LOGIN", username: "", password: "" }));
                    break;
                case "MFA":
                    this.simpleWebSocket.send(JSON.stringify({ type: "MFA", MFA: "123456" }));
                    break;
                case "END":
                    break;
                case "ERROR":
                    console.error(message.detail);
                    break;
                default:
                    console.error(`Invalid message type ${message.type}`);
            }
            if (event.data == "Give Me Info") {
                this.simpleWebSocket.send(JSON.stringify(`{type: "second"}`));
            }
        }

        this.simpleWebSocket.onerror = (event) => {
            console.error(`Error on websocket ${event.data}`);
        }

        this.simpleWebSocket.onclose = (event) => {
            console.log(`Websocket closed`);
        }
    }

    // Get the base path for this script used for urls
    static getBasePath() {
        const url = new URL(import.meta.url)
        console.log(url);
        return `${url.host}`;
    }

    // Utility function that registered the link handler on all links that start necrb
    static initAll(base_path = this.getBasePath()) {
        const buttons = document.querySelectorAll(`button[id="simple-connect"`)
        buttons.forEach(button => new RPASocket(button, base_path).init());
    }

}

export default RPASocket;
