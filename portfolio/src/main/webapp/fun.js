var height = 300;
var width = 500;

var layer1;
var layer2;

var theta1 = 3.14/2;
var theta2 = 2.5;
var omega1 = 0;
var omega2 = 0;
var l1 = 100;
var l2 = 100;
var m1 = 10;
var m2 = 5;
var g = 1;

var x2;
var y2;

running = true;

function setUp() {
  layer1 = document.getElementById("layer1").getContext("2d");
  layer2 = document.getElementById("layer2").getContext("2d");

  reset();
}

function valueChange() {
  reset();

  document.getElementById("m1-val").innerHTML = document.getElementById("m1").value;
  document.getElementById("m2-val").innerHTML = document.getElementById("m2").value;
  document.getElementById("theta1-val").innerHTML = document.getElementById("theta1").value;
  document.getElementById("theta2-val").innerHTML = document.getElementById("theta2").value;
}

function reset() {
  theta1 = parseFloat(document.getElementById("theta1").value);
  theta2 = parseFloat(document.getElementById("theta2").value);
  omega1 = 0;
  omega2 = 0;
  l1 = 100;
  l2 = 100;
  m1 = parseFloat(document.getElementById("m1").value);
  m2 = parseFloat(document.getElementById("m2").value);
  g = 1;

  stop();
  draw();
  layer2.clearRect(0, 0, width, height);
}

function draw() {
  layer1.strokeStyle = "#35A7FF";
  layer1.fillStyle = "#35A7FF";
  layer2.strokeStyle = "#D7263D";

  layer1.beginPath()
  layer2.beginPath()

  layer1.clearRect(0, 0, width, height);

  var offsetX = width/2;
  var offsetY = 50;
  var scale = 1

  var x1 = offsetX+l1*scale*Math.sin(theta1);
  var y1 = offsetY+l1*scale*Math.cos(theta1)

  layer2.moveTo(x2, y2);
  x2 = x1+l2*scale*Math.sin(theta2);
  y2 = y1+l2*scale*Math.cos(theta2);
  layer2.lineTo(x2, y2);
  layer2.stroke();

  layer1.moveTo(offsetX, offsetY);
  layer1.lineTo(x1, y1);
  layer1.stroke();
  
  layer1.beginPath();
  layer1.arc(x1, y1, 10, 0, 2 * Math.PI, true);
  layer1.fill();

  layer1.moveTo(x1, y1);
  layer1.lineTo(x2, y2);
  layer1.stroke();

  layer1.beginPath();
  layer1.arc(x2, y2, 10, 0, 2 * Math.PI, true);
  layer1.fill();

  acc = getAngularAcceleration();
  omega1 += acc.angAcc1;
  omega2 += acc.angAcc2;

  theta1 += omega1;
  theta2 += omega2;

  if (running) {
    setTimeout(draw, 25);
  }
}


// Calculate angular acceleration using these differential equations:
// https://www.myphysicslab.com/pendulum/double-pendulum-en.html
function getAngularAcceleration() {
  var angAcc1 = -g*(2*m1+m2)*Math.sin(theta1) - m2*g*Math.sin(theta1-2*theta2) - 2*Math.sin(theta1-theta2)*m2*(Math.pow(omega2,2)*l2+Math.pow(omega1,2)*l1*Math.cos(theta1-theta2));
  angAcc1 = angAcc1/(l1*(2*m1+m2-m2*Math.cos(2*theta1-2*theta2)));

  var angAcc2 = 2*Math.sin(theta1-theta2)*(Math.pow(omega1, 2)*l1*(m1+m2) + g*(m1+m2)*Math.cos(theta1) + Math.pow(omega2, 2)*l2*m2*Math.cos(theta1-theta2));
  angAcc2 = angAcc2/(l2*(2*m1+m2-m2*Math.cos(2*theta1-2*theta2)));

  return {angAcc1: angAcc1, angAcc2: angAcc2}
}

function start() {
  running = true;
  draw();
}

function stop() {
  running = false;
}