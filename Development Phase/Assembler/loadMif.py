import subprocess

# Paths
assembler_directory = r"path to assembler"
java_file_name = "Assembler"
output_mif_file = r"path to .mif file"

# Compile the Java Assembler
subprocess.run(["javac", f"{assembler_directory}\\{java_file_name}.java"], cwd=assembler_directory, check=True)

# Run the Assembler and redirect the output to the .mif file
with open(output_mif_file, "w") as mif_file:
    subprocess.run(["java", java_file_name], cwd=assembler_directory, stdout=mif_file, text=True, check=True)

print("Assembly complete. The .mif file has been updated.")
