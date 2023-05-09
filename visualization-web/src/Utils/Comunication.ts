

type MessageCallback = (msg: any) => void;

let _messageCallback : MessageCallback | null = null;
let _socket : WebSocket | null = null;
let _alreadyInitialized : boolean = false;

const send = (data : string) => {
    if (_socket === null) {
        throw new Error("Socket not initialized");
    }
    _socket.send(data);
}

const addMessageCallback =  ( processor : MessageCallback) => {
    _messageCallback = processor;
}

const use = ( socket : WebSocket ) => {
    console.log("use");
    socket.addEventListener('error', (data) => {
        console.error("WebSocket:" + data);
    })

    socket.addEventListener('close', (data) => {
        console.error("WebSocket:" + data);
    })

    socket.addEventListener('message', async (message : MessageEvent) => {
        console.log(message);
        if (_messageCallback === null) {
            throw new Error("MessageProccesor not seted");
        }
        _messageCallback(JSON.parse(await message.data));
    })
    
    _socket = socket;

};

const initialize = ( url:string ) => {
    console.log("Initlize: " + url);
    return new Promise<void>( (resolve, reject ) => {
        if (_alreadyInitialized) return resolve();
        console.log("before connect");
        const connection = new WebSocket('ws://localhost:8080/visualization');
        console.log("after connect",connection);

        connection.addEventListener( "connect", () => {
            console.log("connect");
            use(connection)
            resolve();
        })

        connection.addEventListener( "open", () => {
            console.log("open");
            use(connection)
            resolve();
        })

        connection.addEventListener( "error", (error) => {
            console.log(error);
            reject();
        })

    })
}

const Socket = {
    initialize,
    send,
    addMessageCallback,
}

export default Socket;
