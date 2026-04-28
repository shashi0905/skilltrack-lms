# Spring Boot Hot Reload Setup Guide

## ✅ What's Already Configured

I've added Spring Boot DevTools to your project with the following configuration:

### 1. Maven Dependency Added
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

### 2. DevTools Configuration Added to application.yml
```yaml
spring:
  devtools:
    restart:
      enabled: true
      additional-paths: src/main/java
      exclude: static/**,public/**
    livereload:
      enabled: true
      port: 35729
```

## 🚀 How to Use Hot Reload

### Method 1: Using Maven (Recommended)
```bash
cd backend/skilltrack-api
mvn spring-boot:run
```

### Method 2: Using IDE
- **IntelliJ IDEA**: Run the main application class with "Build project automatically" enabled
- **VS Code**: Use Spring Boot Dashboard extension
- **Eclipse**: Run as Spring Boot App

## 🔧 IDE Configuration

### IntelliJ IDEA Setup
1. **Enable Automatic Build**:
   - Go to `File → Settings → Build, Execution, Deployment → Compiler`
   - Check "Build project automatically"

2. **Enable Registry Setting**:
   - Press `Ctrl+Shift+A` (or `Cmd+Shift+A` on Mac)
   - Type "Registry" and open it
   - Find `compiler.automake.allow.when.app.running`
   - Check the box to enable it

3. **Alternative for IntelliJ 2021.2+**:
   - Go to `File → Settings → Advanced Settings`
   - Check "Allow auto-make to start even if developed application is currently running"

### VS Code Setup
1. Install "Spring Boot Extension Pack"
2. Use `Ctrl+Shift+P` → "Spring Boot: Run"
3. Changes will be detected automatically

## 📝 What Gets Hot Reloaded

### ✅ Automatically Reloaded:
- Java class changes (controllers, services, etc.)
- Configuration properties changes
- Resource files (templates, static files)
- Bean definitions and configurations

### ❌ Requires Full Restart:
- Maven dependency changes
- Database schema changes (Flyway migrations)
- Security configuration changes
- Some Spring Boot auto-configuration changes

## 🎯 How It Works

1. **File Change Detection**: DevTools monitors classpath for changes
2. **Automatic Restart**: When changes detected, only your application classes are reloaded
3. **Fast Restart**: Uses two classloaders - base (unchanged) and restart (your code)
4. **LiveReload**: Browser extension can auto-refresh pages

## 🔍 Verification

### Check if DevTools is Active
Look for this log message when starting:
```
INFO --- [  restartedMain] c.s.api.SkillTrackApiApplication : Started SkillTrackApiApplication in X.XXX seconds (JVM running for X.XXX)
```

The `restartedMain` indicates DevTools is active.

### Test Hot Reload
1. Start the application: `mvn spring-boot:run`
2. Make a change to any controller (e.g., add a log statement)
3. Save the file
4. Watch the console - you should see restart logs
5. Test the endpoint - changes should be reflected

## 🚀 Performance Tips

### Exclude Unnecessary Paths
```yaml
spring:
  devtools:
    restart:
      exclude: static/**,public/**,templates/**,META-INF/maven/**,META-INF/resources/**
```

### Trigger Files (Optional)
Create a trigger file to control when restart happens:
```yaml
spring:
  devtools:
    restart:
      trigger-file: .reloadtrigger
```

## 🌐 LiveReload for Frontend

### Browser Extension
1. Install LiveReload browser extension
2. Enable it on your development site
3. Frontend changes will auto-refresh browser

### Manual Trigger
```bash
# Touch any file to trigger reload
touch src/main/resources/.reloadtrigger
```

## 🐛 Troubleshooting

### DevTools Not Working?
1. **Check dependency scope**: Must be `runtime` and `optional`
2. **Verify IDE settings**: Auto-build must be enabled
3. **Check logs**: Look for "restartedMain" in startup logs
4. **Clean and rebuild**: `mvn clean compile`

### Slow Restarts?
1. **Exclude more paths**: Add static resources to exclude list
2. **Use trigger files**: Control when restart happens
3. **Check system resources**: Ensure adequate memory

### Changes Not Detected?
1. **Force compilation**: In IDE, use Build → Rebuild Project
2. **Check file permissions**: Ensure files are writable
3. **Verify classpath**: Changes must be in monitored paths

## 🎉 Ready to Use!

Your Spring Boot application now supports hot reload. Simply:

1. Start with: `mvn spring-boot:run`
2. Make code changes
3. Save files
4. Watch automatic restart in console
5. Test your changes immediately!

## 📊 Expected Performance

- **Cold start**: ~10-15 seconds
- **Hot reload**: ~2-5 seconds
- **File change detection**: ~1-2 seconds

This significantly speeds up development compared to manual restarts!