# CK Metrics Calculator

## Description

CK Metrics Calculator is a fork of [Mauricio Aniche's CK](https://github.com/mauricioaniche/ck)


Use the given artifact to test the tool or to use it as dependency. To build it you need credentials to a private maven repository at the moment. 

## Usage

An example of usage:

```java
String path = "/path/to/project/to/analyze/"
ReducedCK myCk = new ReducedCK();
CKReport report = myCk.calculate(path);
Collection<CKNumber> report = report.all();

for (CKNumber result : allReport) {
			double c3 = ((double) result.getSpecific("C3")) / 1000;
			double cbo = result.getCbo();
			double lcom = result.getLcom();
			double ccbc = ((double) result.getSpecific("CCBC")) / 1000;
			double cr = ((double) result.getSpecific("CR")) / 1000;
			double loc = result.getLoc();
			double wmc = result.getWmc();
			double cd = ((double) result.getSpecific("CD")) / 1000;
			
	\\ do whatever you want with the code metrics for every class in the project
}
```
