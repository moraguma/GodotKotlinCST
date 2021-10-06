extends Spatial


const PIVOT_ROT_SPEED = 0.05


var mind


var left_pressed = false
var right_pressed = false
var x_pressed = false
var y_pressed = false
var z_pressed = false

var total_calls = 0


onready var phone = $Phone
onready var pivot = $CameraPivot
onready var projection = $Projection


func _ready():
	NotificationCenter.create_notification("Started")
	if Engine.has_singleton("VisualizerMind"):
		NotificationCenter.create_notification("Mind located")
		
		mind = Engine.get_singleton("VisualizerMind")
		mind.connect("update_value", NotificationCenter, "update_value")
		mind.connect("create_notification", NotificationCenter, "create_notification")
		mind.start()
		
		NotificationCenter.create_notification("Mind started")
	else:
		NotificationCenter.create_notification("Unable to locate mind")


func _physics_process(delta):
	var rotation_dir = 0
	if left_pressed:
		rotation_dir -= 1
	if right_pressed:
		rotation_dir += 1
	
	pivot.rotate_y(rotation_dir * PIVOT_ROT_SPEED)
	
	if mind != null:
		var basis_string = mind.getBasis()
		var basis = basis_string.rsplit(";")
		var origin_string = mind.getWorldPosition()
		var origin = origin_string.rsplit(";")
		
		if total_calls < 10:
			NotificationCenter.create_notification("Basis: " + str(basis_string) + " - " + str(basis))
			NotificationCenter.create_notification("Origin: " + str(origin_string) + " - " + str(origin))
			total_calls += 1
		
		phone.transform.basis.z = Vector3(float(basis[0]), float(basis[1]), float(basis[2]))
		phone.transform.basis.x = -Vector3(float(basis[3]), float(basis[4]), float(basis[5]))
		phone.transform.basis.y = Vector3(float(basis[6]), float(basis[7]), float(basis[8]))
		phone.transform.origin = Vector3(float(origin[0]), float(origin[2]), float(origin[1]))


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
