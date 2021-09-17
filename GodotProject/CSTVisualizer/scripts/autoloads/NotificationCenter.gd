extends Node2D

onready var VALUES = {"GRAVITY": $Control/Values/Gravity, "ACCELERATION": $Control/Values/Acceleration, "ROTATION": $Control/Values/Rotation, "VELOCITY": $Control/Values/Velocity}
onready var notification_label = $Control/Notification

func create_notification(notification):
	notification_label.text += "\n>" + notification


func update_value(new_value, value_code):
	VALUES[value_code].text = value_code + " - " + new_value
