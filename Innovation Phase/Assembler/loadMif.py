import subprocess


assembler_directory = r"C:/Users/Ahmad/Desktop/github/JoSDC/Innovation Phase/Assembler"
java_file_name = "Assembler"
output_mif_file = r"C:/Users/Ahmad/Desktop/github/JoSDC/Innovation Phase/SuperScalar/dual_issue_inst_memory.mif"


subprocess.run(["javac", f"{assembler_directory}\\{java_file_name}.java"], cwd=assembler_directory, check=True)


with open(output_mif_file, "w") as mif_file:
    subprocess.run(["java", java_file_name], cwd=assembler_directory, stdout=mif_file, text=True, check=True)

print("Assembly complete. The .mif file has been updated.")
