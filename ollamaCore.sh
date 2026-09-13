# ==========================================
# НАСТРОЙКИ СКРИПТА
# ==========================================
# 1. Укажите путь к папке вашего Android-проекта
$projectPath = "C:\AndroidStudioProjects\Calendar4"

# 2. Укажите точное название вашей 7b модели в Ollama (например: llama3, misral, codellama:7b)
$modelName = "$modelName = "qwen2.5-coder:1.5b"" 

# 3. МЕСТО ДЛЯ ВАШЕЙ ЗАДАЧИ (ПРОМПТ ДЛЯ ИИ)
# Четко опишите, что нужно исправить в коде. Попросите возвращать ТОЛЬКО чистый код.
$systemPrompt = @"
Вы — опытный Android Java разработчик. Ваша задача: обновить предоставленный код Java.
ЗАДАЧА: Исправить структуру проекта, обновить устаревшие методы (deprecated) и оптимизировать импорты под стандарты Android Studio.
ВАЖНО: Возвращайте в ответе ТОЛЬКО измененный исходный код файла. Не пишите никаких объяснений, не используйте markdown разметку вроде ```java ... ```. Только чистый Java код.
"@
# ==========================================

# Поиск всех файлов .java в проекте
$javaFiles = Get-ChildItem -Path $projectPath -Filter "*.java" -Recurink -File

# Находим все файлы Kotlin и Java в проекте, исключая папку build
#$files = Get-ChildItem -Path $projectPath -Recurse -Include *.java | Where-Object { $_.FullName -notmatch "[\/\\]build[\/\\]" }


Write-Host "Найдено файлов для обработки: $($javaFiles.Count)" -ForegroundColor Cyan

foreach ($file in $javaFiles) {
    Write-Host "Обработка файла: $($file.FullName)..." -ForegroundColor Yellow
    
    # Чтение исходного кода файла
    $currentCode = Get-Content -Path $file.FullName -Raw
    
    # Формирование запроса к Ollama API (используем эндпоинт generate)
    $body = @{
        model  = $modelName
        prompt = "$systemPrompt`n`nВот исходный код файла:\n`n$currentCode"
        stream = $false
    } | ConvertTo-Json -EnforceArray -Depth 10
    
    try {
        # Отправка запроса в Ollama
        $response = Invoke-RestMethod -Uri "http://localhost:11434/api/generate" -Method Post -Body $body -ContentType "application/json" -TimeoutSec 120
        
        $updatedCode = $response.response
        
        # Минимальная очистка на случай, если нейросеть проигнорировала инструкцию и добавила markdown
        if ($updatedCode -match "```java([\s\S]*?)```") {
            $updatedCode = $Matches[1].Trim()
        } elseif ($updatedCode -match "```([\s\S]*?)```") {
            $updatedCode = $Matches[1].Trim()
        }
        
        # Если ответ не пустой, перезаписываем файл (Git зафиксирует изменения)
        if (![string]::IsNullOrWhiteSpace($updatedCode)) {
            Set-Content -Path $file.FullName -Value $updatedCode -Encoding utf8
            Write-Host "Файл успешно обновлен!" -ForegroundColor Green
        } else {
            Write-Warning "Получен пустой ответ для файла: $($file.Name)"
        }
    }
    catch {
        Write-Error "Ошибка при обработке файла $($file.Name): $_"
    }
    
    # Небольшая пауза между файлами, чтобы не перегревать систему
    Start-Sleep -Seconds 1
}

Write-Host "Обработка завершена! Проверьте изменения в Git (git status / git diff)." -ForegroundColor Green


