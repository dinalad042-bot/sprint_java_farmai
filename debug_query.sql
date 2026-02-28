SELECT u.id_user, u.email, u.role, f.id_ferme, f.nom_ferme, f.id_fermier FROM user u LEFT JOIN ferme f ON u.id_user = f.id_fermier WHERE u.email = 'agricole@farmai.tn' OR u.role = 'AGRICOLE';
