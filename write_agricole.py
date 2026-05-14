import os  
 
filepath = r'src\main\java\tn\esprit\farmai\controllers\AgricoleStatisticsController.java'  
 
# Delete if exists  
if os.path.exists(filepath):  
    os.remove(filepath)  
 
# Write new content  
with open(filepath, 'w', encoding='utf-8') as f:  
    f.write('''package tn.esprit.farmai.controllers;  
  
import javafx.fxml.FXML;  
import tn.esprit.farmai.utils.SessionManager;  
  
public class Test { }  
''')  
 
print('Done')  
