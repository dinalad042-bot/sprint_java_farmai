#!/bin/bash

# FarmAI Database Setup Script
# This script helps set up the database for manual testing

echo "🚀 FarmAI Database Setup Script"
echo "══════════════════════════════════════"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if MySQL is running
echo "🔍 Checking MySQL status..."
if pgrep -x "mysqld" > /dev/null; then
    echo -e "${GREEN}✅ MySQL is running${NC}"
else
    echo -e "${RED}❌ MySQL is not running${NC}"
    echo "Please start MySQL first: sudo service mysql start"
    exit 1
fi

# Check if we can connect to MySQL
echo ""
echo "🔌 Testing MySQL connection..."
mysql -u root -e "SELECT 1;" 2>/dev/null
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ MySQL connection successful${NC}"
else
    echo -e "${RED}❌ MySQL connection failed${NC}"
    echo "Please check MySQL credentials and ensure it's accessible"
    exit 1
fi

# Create database if it doesn't exist
echo ""
echo "📁 Creating database..."
mysql -u root -e "CREATE DATABASE IF NOT EXISTS farmai DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
echo -e "${GREEN}✅ Database 'farmai' created/verified${NC}"

# Run the SQL script
echo ""
echo "📋 Setting up database schema..."
mysql -u root farmai < ../database/farmai_complete.sql
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Database schema setup completed${NC}"
else
    echo -e "${RED}❌ Database schema setup failed${NC}"
    exit 1
fi

# Verify the setup
echo ""
echo "📊 Verifying database setup..."
USER_COUNT=$(mysql -u root farmai -e "SELECT COUNT(*) FROM user;" -B -N)
echo -e "${GREEN}✅ Found $USER_COUNT users in database${NC}"

FARM_COUNT=$(mysql -u root farmai -e "SELECT COUNT(*) FROM ferme;" -B -N)
echo -e "${GREEN}✅ Found $FARM_COUNT farms in database${NC}"

echo ""
echo -e "${GREEN}🎉 Database setup completed successfully!${NC}"
echo ""
echo "🔑 Test login credentials:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Admin: admin@farmai.tn / password123"
echo "Expert: expert@farmai.tn / password123"
echo "Agricole: agricole@farmai.tn / password123"
echo "Fournisseur: fournisseur@farmai.tn / password123"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "🎮 You can now start the FarmAI application!"
echo "Run: mvn javafx:run"