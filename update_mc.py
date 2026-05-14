import os  
filepath = 'src/main/java/tn/esprit/farmai/controllers/MesCulturesController.java'  
with open(filepath, 'r', encoding='utf-8') as f:  
    content = f.read()  
content = content.replace('import javafx.scene.shape.Circle;', 'import javafx.scene.shape.Circle;\nimport javafx.scene.image.ImageView;\nimport tn.esprit.farmai.utils.AvatarUtil;')  
with open(filepath, 'w', encoding='utf-8') as f:  
    f.write(content)  
print('Done') 
