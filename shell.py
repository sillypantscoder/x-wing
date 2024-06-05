import os
import subprocess

# 1. Get the files to compile

files: "list[str]" = []
def checkfolder(folder):
	for file in os.listdir(folder):
		newname = os.path.join(folder, file)
		if os.path.isdir(newname):
			checkfolder(newname)
		else:
			if newname.split(".")[-1] in ["java", "html"]:
				files.append(newname)
checkfolder("src")

# 2. Compile the files

print("Compiling...")
os.makedirs("compiled_output", exist_ok=True)
cmd = ["javac", "-g", "-d", "compiled_output", "-cp", "src"]
for filename in files:
	if filename.endswith(".java"):
		cmd.append(filename)
	else:
		subprocess.run(["mkdir", "-p", "compiled_output/" + filename[4:filename.rfind("/")]])
		subprocess.run(["cp", filename, "compiled_output/" + filename[4:]])
subprocess.run(cmd)

# 3. Start jshell

subprocess.run(["jshell", "--class-path=compiled_output"])

subprocess.run(["rm", "-r", "compiled_output"])
