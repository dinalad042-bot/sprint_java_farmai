# FarmAI Manual Testing Guide

## 🚀 Quick Start

1. **Setup Database**: Run database initialization
2. **Start Application**: Launch FarmAI
3. **Test Features**: Follow the testing scenarios below

---

## 📋 Database Setup

### Option 1: Automatic (Recommended)
The application will automatically initialize the database when you run it:
```bash
mvn javafx:run
```

### Option 2: Manual Setup
```bash
# Windows
scripts\setup-database.bat

# Linux/Mac
chmod +x scripts/setup-database.sh
./scripts/setup-database.sh
```

### Option 3: Java Program
```bash
mvn compile exec:java -Dexec.mainClass="tn.esprit.farmai.DatabaseInitializer"
```

---

## 🔑 Test Credentials

| Role | Email | Password | Features |
|------|-------|----------|----------|
| **Admin** | admin@farmai.tn | password123 | All features |
| **Expert** | expert@farmai.tn | password123 | Analysis, Conseils, Statistics |
| **Agricole** | agricole@farmai.tn | password123 | Farm management, Animals, Plants |
| **Fournisseur** | fournisseur@farmai.tn | password123 | Supplier dashboard |

---

## 🧪 Testing Scenarios

### 1. Authentication Testing

#### Standard Login
1. Open application → Login screen appears
2. Enter credentials → Click "Se connecter"
3. Expected: Dashboard loads based on user role

#### Face Recognition Login
1. Click "Se connecter avec le visage" button
2. Position face in camera view
3. Expected: Automatic login if face recognized

#### Wrong Credentials
1. Enter invalid email/password
2. Click "Se connecter"
3. Expected: Error message displayed

### 2. Farm Management Testing

#### Create Farm
1. Login as Agricole → Navigate to "Gestion Fermes"
2. Click "Ajouter" → Fill farm details
3. Expected: New farm appears in list

#### View Animals
1. Navigate to "Gestion Animaux"
2. Expected: List of animals with details

#### Add Animal
1. Click "Ajouter Animal"
2. Fill: Species, Health Status, Birth Date, Farm
3. Expected: Animal added successfully

#### View Plants
1. Navigate to "Gestion Plantes"
2. Expected: List of plants with details

#### Add Plant
1. Click "Ajouter Plante"
2. Fill: Species, Life Cycle, Farm, Quantity
3. Expected: Plant added successfully

### 3. Expert Features Testing

#### Create Analysis
1. Login as Expert → Navigate to "Gestion Analyses"
2. Click "Ajouter Analyse"
3. Fill: Technical Result, Farm, Technician
4. Expected: Analysis created successfully

#### Add Conseil
1. Navigate to "Gestion Conseils"
2. Click "Ajouter Conseil"
3. Fill: Description, Priority, Analysis
4. Expected: Conseil created successfully

#### View Statistics
1. Navigate to "Tableau de bord Expert"
2. Expected: Charts and statistics displayed

### 4. Security Features Testing

#### User Activity Log
1. Login as Admin → Navigate to "User Logs"
2. Expected: List of user activities

#### OTP Verification
1. Try face login or sensitive operations
2. Expected: OTP sent to email

### 5. PDF Generation Testing

#### Generate Analysis Report
1. Navigate to "Gestion Analyses"
2. Select an analysis → Click "Générer PDF"
3. Expected: PDF report downloaded

#### Generate Farm Report
1. Navigate to "Gestion Fermes"
2. Select a farm → Click "Générer Rapport"
3. Expected: PDF farm report generated

---

## 🔍 Data Verification

### Database State Check
```sql
-- Check user count
SELECT COUNT(*) FROM user;

-- Check farm count
SELECT COUNT(*) FROM ferme;

-- Check animal count
SELECT COUNT(*) FROM animaux;

-- Check plant count
SELECT COUNT(*) FROM plantes;

-- Check analysis count
SELECT COUNT(*) FROM analyse;

-- Check conseil count
SELECT COUNT(*) FROM conseil;
```

### Expected Data Counts
- **Users**: 4 (Admin, Expert, Agricole, Fournisseur)
- **Farms**: 6+ test farms
- **Animals**: 6+ test animals
- **Plants**: 8+ test plants
- **Analyses**: 5+ test analyses
- **Conseils**: 5+ test conseils

---

## 🐛 Common Issues & Solutions

### Database Connection Issues
**Problem**: "Connexion etablie!" not showing
**Solution**: 
1. Ensure MySQL is running
2. Check connection in `MyDBConnexion.java`
3. Verify database exists

### Face Recognition Not Working
**Problem**: Camera not detected or face not recognized
**Solution**:
1. Check camera permissions
2. Ensure JavaCV dependencies are loaded
3. Test in good lighting conditions

### Compilation Errors
**Problem**: Build fails
**Solution**:
```bash
mvn clean compile
```

### Missing Dependencies
**Problem**: Class not found errors
**Solution**:
```bash
mvn dependency:resolve
```

---

## 📊 Test Data Summary

### Test Farms
- Ferme Pilote (Tunis)
- Ferme Sfax (Sfax)
- Ferme Sousse (Sousse)
- Ferme Test Nord
- Ferme Test Sud
- Ferme Test Est

### Test Animals
- Sheep (Mouton) - 2 animals
- Goats (Chèvre) - 2 animals
- Cows (Vache) - 2 animals

### Test Plants
- Vegetables: Tomate, Carotte, Laitue
- Fruits: Pommier, Oranger, Olivier
- Cereals: Blé, Orge

### Test Analyses
- Soil analysis
- Plant health analysis
- Water quality analysis
- Animal health analysis
- Crop yield analysis

### Test Conseils
- High priority (URGENT/IMPORTANT)
- Medium priority (RECOMMENDED)
- Low priority (TO CONSIDER)

---

## 🎯 Manual Testing Checklist

- [ ] Application starts without errors
- [ ] Database connection established
- [ ] Login with all 4 user roles
- [ ] Face recognition login (if camera available)
- [ ] Farm CRUD operations
- [ ] Animal CRUD operations
- [ ] Plant CRUD operations
- [ ] Analysis CRUD operations
- [ ] Conseil CRUD operations
- [ ] Statistics dashboard loads
- [ ] PDF generation works
- [ ] User activity logging
- [ ] All dashboards accessible
- [ ] No console errors
- [ ] All features responsive

---

## 🚀 Next Steps

After successful manual testing:
1. **Deploy to production**
2. **Train end users**
3. **Monitor performance**
4. **Collect feedback**
5. **Plan enhancements**