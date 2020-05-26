FROM hmctspublic.azurecr.io/base/java:openjdk-11-distroless-1.2

COPY lib/AI-Agent.xml /opt/app/
COPY build/libs/bulk-scan-end-to-end-tests.jar /opt/app/

CMD [ "bulk-scan-end-to-end-tests.jar" ]
