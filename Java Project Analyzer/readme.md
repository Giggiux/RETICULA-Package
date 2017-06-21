# Java Project Analyzer

To build it you need credentials to a private maven repository at the moment. Or you can publish the *CK Metrics Calculator* in your local maven repository and run
`sbt run` with the following Environment Variables:

```
SQL_SERVER 		# Database address of format: jdbc:postgresql://ADDRESS:PORT
SQL_USER		# Db User  (needs write privileges on the db)       
SQL_PASSWORD	# Db Password
SQL_DB			# Db Name
GITS_LIST		# Path of the JSON file with the list of the projects to analyze (we provide one, in the root of this package calles `gits.json`) 

```