extends Spatial


const PIVOT_ROT_SPEED = 0.05
const SPEED = 1


var mind


var device_quat = Quat()
var device_acceleration = Vector3()
var velocity = Vector3(0, 0, 0)
var origin = null

var left_pressed = false
var right_pressed = false
var x_pressed = false
var y_pressed = false
var z_pressed = false


onready var phone = $Phone
onready var pivot = $CameraPivot
onready var projection = $Projection


func _ready():
	NotificationCenter.create_notification("Started")
	if Engine.has_singleton("VisualizerMind"):
		NotificationCenter.create_notification("Mind located")
		
		mind = Engine.get_singleton("VisualizerMind")
		mind.connect("update_value", NotificationCenter, "update_value")
		mind.connect("update_rotation", self, "update_rotation")
		mind.connect("update_acceleration", self, "update_acceleration")
		mind.start()
		
		NotificationCenter.create_notification("Mind started")
	else:
		NotificationCenter.create_notification("Unable to locate mind")


func update_rotation(x, y, z, w):
	device_quat = Quat(float(w), float(x), float(z), float(y))
	NotificationCenter.update_value(str(device_quat), "ROTATION")
	
	if origin == null:
		origin = device_quat.inverse()
	
	var t_aux = phone.translation
	phone.transform = Transform(origin * device_quat)
	phone.translation = t_aux


func update_acceleration(x, y, z):
	device_acceleration = Vector3(-float(x), -float(z), -float(y))


func _physics_process(delta):
	var rotation_dir = 0
	if left_pressed:
		rotation_dir -= 1
	if right_pressed:
		rotation_dir += 1
	
	pivot.rotate_y(rotation_dir * PIVOT_ROT_SPEED)
	
	var dir = Vector3(0, 0, 0)
	if x_pressed:
		dir += Vector3(1, 0, 0)
	if y_pressed:
		dir += Vector3(0, 1, 0)
	if z_pressed:
		dir += Vector3(0, 0, 1)
	
	phone.translate(dir * SPEED * delta)
	
	projection.translation = Vector3(phone.transform.origin[0], 0, phone.transform.origin[2])


func left_pressed_down():
	left_pressed = true


func left_pressed_up():
	left_pressed = false


func right_pressed_down():
	right_pressed = true


func right_pressed_up():
	right_pressed = false


func x_pressed_down():
	x_pressed = true


func x_pressed_up():
	x_pressed = false


func y_pressed_down():
	y_pressed = true


func y_pressed_up():
	y_pressed = false


func z_pressed_down():
	z_pressed = true


func z_pressed_up():
	z_pressed = false
