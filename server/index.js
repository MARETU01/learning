const express = require('express');
const path = require('path');
const http = require('http');
const socketIo = require('socket.io');
const app = express();
const server = http.createServer(app);
const io = socketIo(server);
const port = 3000;
const bodyParser = require('body-parser');

app.use(express.static('static'));
const fs = require('fs');

app.use(bodyParser.urlencoded({ extended: true }));

io.on('connection', (socket) => {
    console.log('A user connected');

    fs.readFile('static/leaderboard.txt', 'utf8', (err, data) => {
        if (err) {
            console.error(err);
            return;
        }
        // Send leaderboard data to the client
        io.emit('leaderboard', data);
    });
    socket.on('disconnect', () => {
        console.log('A user disconnected');
    });
});

// Use for writing leaderboard
app.post('/quiz', (req, res) => {
    let username = req.body.username;
    let count = req.body.score;
    let time = req.body.time;
    const data = `${username};${count};${time}\n`;
    fs.appendFile('static/leaderboard.txt', data, () => {
        console.log('Data saved to file.');
    });
    res.sendFile(path.join(__dirname, 'leaderboard.html'))
});

// Root(introduction) page
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'introduction.html'));
});

// About page
app.get('/about', (req, res) => {
    res.sendFile(path.join(__dirname, 'about.html'));
});

// Quiz page
app.get('/quiz', (req, res) => {
    res.sendFile(path.join(__dirname, 'quiz.html'));
});

// Start server
server.listen(port, () => {
    console.log(`Server is running at http://localhost:${port}`);
});