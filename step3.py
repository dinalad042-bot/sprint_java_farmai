import os  
filepath = 'src/main/java/tn/esprit/farmai/controllers/MesCulturesController.java'  
with open(filepath, 'r', encoding='utf-8') as f:  
    content = f.read()  
old = 'ProfileManager.updateProfileUI(currentUser, welcomeLabel, userNameLabel, sidebarAvatar, sidebarAvatarText);'  
new = '''ProfileManager.updateProfileUI(currentUser, welcomeLabel, userNameLabel, sidebarAvatar, sidebarAvatarText);\n\n            // Load header profile image\n            if (headerProfileImage != null) {\n                AvatarUtil.loadUserImageIntoImageView(headerProfileImage, currentUser, 36);\n            }'''  
content = content.replace(old, new, 1)  
with open(filepath, 'w', encoding='utf-8') as f:  
    f.write(content)  
print('Initialize updated')  
