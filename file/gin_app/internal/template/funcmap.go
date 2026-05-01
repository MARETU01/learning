package template

import (
	"fmt"
	"html/template"
	"math"
	"net/url"

	"gin_app/internal/utils"
)

// 模板自定义函数
func FuncMap() template.FuncMap {
	return template.FuncMap{
		"urlEncode": func(s string) string {
			return url.PathEscape(s)
		},
		"formatSize": func(size int64) string {
			return utils.FormatFileSize(size)
		},
		"roundFloat": func(val float64, precision int) float64 {
			pow := math.Pow10(precision)
			return math.Round(val*pow) / pow
		},
		"safe": func(s string) template.HTML {
			return template.HTML(s)
		},
		"add": func(a, b int64) int64 {
			return a + b
		},
		"mul": func(a, b interface{}) float64 {
			return toFloat(a) * toFloat(b)
		},
		"div": func(a, b interface{}) float64 {
			bf := toFloat(b)
			if bf == 0 {
				return 0
			}
			return toFloat(a) / bf
		},
		"gt": func(a, b float64) bool {
			return a > b
		},
		"fmtFloat": func(val float64, format string) string {
			return fmt.Sprintf(format, val)
		},
		"round": func(val float64, precision int) float64 {
			pow := math.Pow10(precision)
			return math.Round(val*pow) / pow
		},
	}
}

func toFloat(v interface{}) float64 {
	switch val := v.(type) {
	case int:
		return float64(val)
	case int64:
		return float64(val)
	case uint64:
		return float64(val)
	case float64:
		return val
	default:
		return 0
	}
}
