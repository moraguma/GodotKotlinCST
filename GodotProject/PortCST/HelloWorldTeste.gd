extends Node2D


onready var rich_text_label = $RichTextLabel
onready var label = $Label

var HelloWorld

func _ready():
	rich_text_label.text += "Funcionando\n"
	if Engine.has_singleton("HelloWorld"):
		rich_text_label.text += "Singleton identificado\n"
		HelloWorld = Engine.get_singleton("HelloWorld")
		HelloWorld.connect("hello", self, "_on_hello")
		HelloWorld.sendSignal("AUAUAU")

func _on_hello(value):
	label.text = value


func _on_Button_pressed():
	if HelloWorld != null:
		var s = HelloWorld.hello()
		rich_text_label.text += s + "\n"
