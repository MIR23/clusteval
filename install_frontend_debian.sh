#!/bin/bash

RUBY_VERSION='1.9.1'
INSTALL_DIRECTORY='$HOME/ClustEvalWebBase'
MYSQL_ROOT_USER='root'
MYSQL_NEW_WRITE_USER='clustEvalWrite'
MYSQL_NEW_WRITE_USER_PW='clustEvalWrite'
MYSQL_NEW_READ_USER='clustEvalRead'
MYSQL_NEW_READ_USER_PW='clustEvalRead'
# website does rely on this database name
DB_NAME='clustEval'

# install required aptitude packages
echo 'Installing aptitude packages...'
sudo apt-get install apache2 ruby$RUBY_VERSION-full git mysql-server libxml2-dev libxslt1-dev python g++ make libcurl4-openssl-dev libssl-dev zlib1g-dev apache2-prefork-dev libapr1-dev libaprutil1-dev

# create symlinks for installed binaries
echo 'Setting up symlinks..'
sudo ln -s /usr/bin/ruby$RUBY_VERSION /usr/bin/ruby
sudo ln -s /usr/bin/gem$RUBY_VERSION /usr/bin/gem
sudo ln -s /var/lib/gems/$RUBY_VERSION/bin/bundle /usr/bin/bundle

# install required gems
echo 'Install required gems'
sudo gem install bundler passenger

# install nodejs, javascript runtime engine
#echo 'Installing Javascript Engine node.js'
#mkdir /tmp/nodejs_tmp_install && cd $_
#wget -N http://nodejs.org/dist/node-latest.tar.gz
#tar xzvf node-latest.tar.gz && cd `ls -rd node-v*`
#./configure
#sudo make install

# create mysql user and database
echo 'Configuration MYSQL Database...'
echo '(You will be asked for your mysql password three times)'
mysql -u $MYSQL_ROOT_USER -p --execute "GRANT ALL PRIVILEGES ON *.* TO '$MYSQL_NEW_WRITE_USER'@'localhost' IDENTIFIED BY '$MYSQL_NEW_WRITE_USER_PW' WITH GRANT OPTION;"
mysql -u $MYSQL_ROOT_USER -p --execute "GRANT ALL PRIVILEGES ON *.* TO '$MYSQL_NEW_READ_USER'@'localhost' IDENTIFIED BY '$MYSQL_NEW_READ_USER_PW' WITH GRANT OPTION;"
mysql -u $MYSQL_ROOT_USER -p --execute "CREATE DATABASE $DB_NAME;" 

# checkout website
echo 'Checking out Website into $INSTALL_DIRECTORY ...'
git clone https://github.com/wiwie/clustevalWebsite $INSTALL_DIRECTORY
cd $INSTALL_DIRECTORY/clustevalWebsite
sudo rm -R $INSTALL_DIRECTORY/tmp
/var/lib/gems/$RUBY_VERSION/bin/rake tmp:create

# install gems required by frontend
echo 'Installing gems required by website...'
sudo bundle install --no-deployment
sudo bundle install --deployment

# init DB of frontend
echo 'initialize database contents'
sudo bundle exec rake db:reset

# install passenger apache2 module
echo 'Installing passenger apache2 module'
sudo /var/lib/gems/$RUBY_VERSION/gems/passenger-*/bin/passenger-install-apache2-module

# create virtualhost file
clustevalSiteFile=/etc/apache2/sites-available/ClustEval
sudo touch $clustevalSiteFile
echo "<VirtualHost *:80>" | sudo tee -a $clustevalSiteFile
echo "      ServerName localhost" | sudo tee -a $clustevalSiteFile
echo "      DocumentRoot $INSTALL_DIRECTORY/public    " | sudo tee -a $clustevalSiteFile
echo "      <Directory $INSTALL_DIRECTORY/public>" | sudo tee -a $clustevalSiteFile
echo "         # This relaxes Apache security settings." | sudo tee -a $clustevalSiteFile
echo "         AllowOverride all" | sudo tee -a $clustevalSiteFile
echo "         # MultiViews must be turned off." | sudo tee -a $clustevalSiteFile
echo "         Options -MultiViews" | sudo tee -a $clustevalSiteFile
echo "      </Directory>" | sudo tee -a $clustevalSiteFile
echo "   </VirtualHost>" | sudo tee -a $clustevalSiteFile

sudo a2ensite ClustEval

echo '###########'
echo 'A virtualhost file for clusteval has been automatically created and enabled.'
echo 'Please follow the instructions above, on how to modify "/etc/apache2/apache2.conf"!'
echo 'Restart your apache server afterwards by executing "sudo apache2ctl restart"'
