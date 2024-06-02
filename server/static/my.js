function toPlay() {
    var audio = document.getElementById('bgm');
    var icon = document.getElementById('bgm-icon');
    if (audio.paused) {
        audio.play();
        icon.src = "/on.png";
    } else {
        audio.pause();
        icon.src = "/off.png";
    }
}

let currentImageIndex = 1;
let images = [
    "/hobby1.jpg",
    "/hobby2.jpg",
    "/hobby3.jpg",
    "/hobby4.jpg"
];

function next() {
  currentImageIndex = (currentImageIndex + 1) % images.length;
  document.getElementById('currentImage').src = images[currentImageIndex];
}

function startAutoSwitch() {
  setInterval(next, 5000);  // Switch images every 5 seconds
}

window.onload = function() {  // Automatically switch after page loading is completed
  startAutoSwitch();
};

const answers = ['C', 'B', 'C', 'B', 'A', 'D', 'B', 'A', 'D', 'D'];
let username = "";  // Initialize the user's name and answer status
let process = [];
let time = [];
let startTime = null;
let currentTimer;  // Define the timer references in a broader scope

function checkName() {
    var playerName = document.getElementById('name').value;
    if (playerName.trim() === '') {
        alert("Please enter your name before starting the quiz.");
        return;
    }
    username = playerName;
    document.getElementById('start').style.display = 'none';
    document.getElementById('1').style.display = 'block';
    startTime = new Date();
    startQuestionTimer();
}

function startQuestionTimer() {
    let countdown = 15;
    let countdownElement = document.getElementById('countdown');
    document.getElementById('timer').style.display = 'block'; // Display the Timer
    countdownElement.textContent = countdown;
    currentTimer = setInterval(() => {
        countdown--;
        countdownElement.textContent = countdown;
        if (countdown <= 0) {
            clearInterval(currentTimer);
            check('E');
        }
    }, 1000);
}

function check(option) {
    document.getElementById('timer').style.display = 'none';
    let endTime = new Date();
    clearInterval(currentTimer);
    if (option === 'E') {
        time.push(15);
    }
    else {
        time.push((endTime - startTime) / 1000);
    }
    let questionId = process.length + 1;
    document.getElementById(questionId.toString()).style.display = 'none';
    if (answers[questionId - 1] === option) {
        process.push('T');
        document.getElementById('correct').style.display = 'block';
    } else {
        process.push('F');
        document.getElementById('wrong').style.display = 'block';
    }
    if (process.length == 10) {
        final();
    }
}

function nextQuestion(element) {
    let question = process.length + 1;
    element.closest('div').style.display = 'none';
    document.getElementById(question.toString()).style.display = 'block';
    startTime = new Date();
    startQuestionTimer();
}

function final() {
    let transition = document.getElementsByClassName('transition')[0];
    let buttonToRemove = transition.querySelector('button');
    buttonToRemove.remove();
    let count = 0;
    for (let item of process) {
        if (item === 'T') {
            count += 1;
        }
    }
    let completionMessage = document.createElement('h4');
    completionMessage.textContent = "You completed all the questions!";
    
    let lineBreak = document.createElement('br');
    
    let scoreMessage = document.createElement('h4');
    scoreMessage.textContent = `You got ${count} right out of ${process.length}`;

    transition.appendChild(completionMessage);
    transition.appendChild(scoreMessage);
    transition.appendChild(lineBreak.cloneNode());

    document.getElementById('username').value = username;
    document.getElementById('score').value = count;

    let sumTime = 0;
    for (let num of time) {
        sumTime += num;
    }
    document.getElementById('time').value = sumTime.toFixed(2);  // Limit to two decimal places
    document.getElementById('form').style.display = 'block';
}