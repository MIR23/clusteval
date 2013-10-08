# append 'deb http://cran.cnr.berkeley.edu/bin/linux/debian squeeze-cran/' to /etc/apt/sources.list
# import the public key of the new server by executing
# gpg --keyserver subkeys.pgp.net --recv-keys 06F90DE5381BA480
# gpg -a --export 06F90DE5381BA480 | sudo apt-key add -
# sudo apt-get update
# sudo apt-get install r-base 

# sudo apt-get install openjdk-6-jre

# sudo R
# install.packages('Rserve')

# svn export https://svn.csb.mpi-inf.mpg.de/CSB/ClustEval/trunk/packages --username wiwiec
# cd packages
# java -jar ClustEvalFramework_Server.jar
# java -jar ClustEvalFramework_Client.jar
# svn export https://svn.csb.mpi-inf.mpg.de/CSB/ClustEval/trunk/repository --username wiwiec