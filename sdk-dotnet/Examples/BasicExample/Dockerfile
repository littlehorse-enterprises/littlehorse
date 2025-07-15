FROM mcr.microsoft.com/dotnet/sdk:8.0 AS base
COPY ./BasicExample/ src/
WORKDIR /src
RUN dotnet build
CMD ["dotnet", "run"]