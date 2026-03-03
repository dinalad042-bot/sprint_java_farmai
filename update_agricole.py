import os  
  
filepath = r'src\main\java\tn\esprit\farmai\controllers\AgricoleStatisticsController.java'  
  
with open(filepath, 'r', encoding='utf-8') as f:  
    current = f.read()  
print('Current length:', len(current))  
