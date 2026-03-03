import os 
filepath = 'src/main/java/tn/esprit/farmai/controllers/MesCulturesController.java' 
with open(filepath, 'r', encoding='utf-8') as f: 
    lines = f.readlines() 
new_lines = []  
i = 0  
while i < len(lines):  
    line = lines[i] 
