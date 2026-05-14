import re  
  
with open('src/main/java/tn/esprit/farmai/controllers/AgricoleStatisticsController.java', 'r') as f:  
    content = f.read()  
  
print('Original length:', len(content)) 
