
const SERVER_IP = '10.19.80.28'; // 你电脑当前的局域网 IP（手机热点）

export default {
    baseURL: 'http://' + SERVER_IP + ':10010',
    wsURL: 'ws://' + SERVER_IP + ':8080/ws',
};