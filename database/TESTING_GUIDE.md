# FarmAI Database Testing Guide

## 📊 Database Import Instructions

### For phpMyAdmin:
1. Open phpMyAdmin in your browser
2. Click on the **"Databases"** tab
3. Create a new database named `farmai` (or let the script create it)
4. Click on the `farmai` database
5. Click on the **"Import"** tab
6. Click **"Choose file"** and select `farmai_complete_with_data.sql`
7. Click **"Go"** to import

### For MySQL Command Line:
```bash
mysql -u your_username -p your_database_name < farmai_complete_with_data.sql
```

## 🔑 Test User Credentials

All users have the password: **`password123`**

| Email | Role | CIN | Description |
|-------|------|-----|-------------|
| `admin@farmai.tn` | ADMIN | 00000000 | Full system access |
| `expert@farmai.tn` | EXPERT | 11111111 | Can create analyses and conseils |
| `expert1@farmai.tn` | EXPERT | 44444444 | Additional expert user |
| `senior@farmai.tn` | EXPERT | 77777777 | Senior expert user |
| `agricole@farmai.tn` | AGRICOLE | 22222222 | Farm owner with animals/plants |
| `ahmed@farmai.tn` | AGRICOLE | 55555555 | Farmer with multiple farms |
| `fatma@farmai.tn` | AGRICOLE | 66666666 | Female farmer |
| `jeune@farmai.tn` | AGRICOLE | 88888888 | Young farmer |
| `fournisseur@farmai.tn` | FOURNISSEUR | 33333333 | Supplier access |
| `principal@farmai.tn` | FOURNISSEUR | 99999999 | Principal supplier |

## 🧪 Feature Testing Checklist

### Login & Security Features
- [ ] Standard login with email/password
- [ ] Face recognition login (if camera available)
- [ ] OTP verification
- [ ] User session management
- [ ] User activity logging

### Admin Features
- [ ] User management (CRUD operations)
- [ ] Role assignment
- [ ] System configuration
- [ ] User activity monitoring

### Expert Features
- [ ] Create new analyses
- [ ] Add technical results with images
- [ ] Create conseils (advice) with priority levels
- [ ] Expert dashboard with statistics
- [ ] Analysis management interface

### Farm Management Features
- [ ] Farm CRUD operations
- [ ] Animal management (10 different animals)
- [ ] Plant management (10 different plants)
- [ ] Farm assignment to farmers

### Dashboard Features
- [ ] Admin dashboard
- [ ] Expert dashboard with statistics
- [ ] Agricole dashboard
- [ ] Fournisseur dashboard
- [ ] User list management
- [ ] Notification system

## 📈 Sample Data Summary

| Entity | Count | Description |
|--------|-------|-------------|
| Users | 10 | All roles represented |
| Farms | 5 | Different locations and sizes |
| Animals | 10 | Various species and health states |
| Plants | 10 | Different crops and life cycles |
| Analyses | 8 | Technical analyses with results |
| Conseils | 8 | Expert advice with priorities |
| User Logs | 8 | Activity tracking entries |

## 🗃️ Database Tables Created

1. **user** - User accounts and profiles
2. **ferme** - Farm information and details
3. **analyse** - Technical analyses performed
4. **conseil** - Expert advice/recommendations
5. **animaux** - Animal inventory and health
6. **plantes** - Plant inventory and cultivation
7. **face_data** - Face recognition models
8. **user_log** - User activity logging

## 🔗 Table Relationships

- Users can own multiple farms (1-to-many)
- Farms can have multiple animals and plants (1-to-many)
- Analyses are linked to farms and technicians (many-to-1)
- Conseils are linked to analyses (many-to-1)
- User logs track user activities (many-to-1)
- Face data is unique per user (1-to-1)

## 🚀 Quick Start Testing

1. **Import the database** using phpMyAdmin or MySQL CLI
2. **Start the application** (`mvn javafx:run`)
3. **Login with admin@farmai.tn / password123**
4. **Test all dashboards** and features
5. **Verify data integrity** - all sample data should be visible

## 📋 Test Scenarios

### Scenario 1: Expert Workflow
1. Login as `expert@farmai.tn`
2. Navigate to expert dashboard
3. Create a new analysis for a farm
4. Add a conseil (advice) for that analysis
5. Check statistics dashboard

### Scenario 2: Farm Management
1. Login as `agricole@farmai.tn`
2. Go to farm management
3. View animals and plants
4. Add/edit animal information
5. Check plant inventory

### Scenario 3: Security Testing
1. Login as any user
2. Check user logs show your activity
3. Try face login (if available)
4. Check session management

## 🐛 Common Issues & Solutions

### Issue: Database connection failed
**Solution**: Check MySQL credentials in `MyDBConnexion.java`

### Issue: Tables not created
**Solution**: Ensure you're using the `farmai_complete_with_data.sql` file, not the basic schema

### Issue: Login fails
**Solution**: Use the exact email addresses and the password `password123`

### Issue: Foreign key constraint errors
**Solution**: Import the SQL file in order - don't run individual inserts out of sequence

## 📊 Verification Queries

Run these in phpMyAdmin to verify your data:

```sql
-- Count all records
SELECT 'Users:' AS table_name, COUNT(*) FROM user UNION ALL
SELECT 'Farms:', COUNT(*) FROM ferme UNION ALL
SELECT 'Animals:', COUNT(*) FROM animaux UNION ALL
SELECT 'Plants:', COUNT(*) FROM plantes UNION ALL
SELECT 'Analyses:', COUNT(*) FROM analyse UNION ALL
SELECT 'Conseils:', COUNT(*) FROM conseil;

-- Check user roles
SELECT role, COUNT(*) as count FROM user GROUP BY role;

-- Check farm assignments
SELECT u.email, f.nom_ferme, f.lieu 
FROM user u 
JOIN ferme f ON u.id_user = f.id_fermier 
ORDER BY u.email;
```

---

**Happy Testing!** 🎉
Your FarmAI application should now have a fully populated database ready for comprehensive testing of all integrated features.