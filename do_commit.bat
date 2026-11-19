@echo off
cd /d "c:\Users\Zahid Argumedo\Documents\Ecomerce_CafeValleBlanco"
git add Frontend/src/app/modules/admin/layout-admin/layout-admin.scss
git add Frontend/src/app/modules/admin/pages/dashboard/dashboard.html
git add Frontend/src/app/modules/admin/pages/dashboard/dashboard.scss
git status --short
git commit -F commit_msg.txt
git push origin new_dev
echo DONE
