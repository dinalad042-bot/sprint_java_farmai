from pathlib import Path  
p = Path('src/main/resources/tn/esprit/farmai/views/mes-cultures.fxml')  
c = p.read_text(encoding='utf-8')  
lines = c.split("\n")  
  
new_lines = []  
i = 0  
while i < len(lines):  
    line = lines[i]  
