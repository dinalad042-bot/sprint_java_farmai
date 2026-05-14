import os  
filepath = 'src/main/java/tn/esprit/farmai/controllers/MesCulturesController.java'  
with open(filepath, 'r', encoding='utf-8') as f:  
    content = f.read()  
content = content.replace('private Circle headerAvatarCircle;', 'private ImageView headerProfileImage;')  
with open(filepath, 'w', encoding='utf-8') as f:  
    f.write(content)  
print('Field updated')  
